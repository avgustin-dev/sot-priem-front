package kg.sot.reception.service;

import kg.sot.reception.dto.BookAppointmentRequest;
import kg.sot.reception.dto.BookAppointmentResponse;
import kg.sot.reception.dto.CancelRequest;
import kg.sot.reception.dto.CitizenActionRequest;
import kg.sot.reception.dto.Companion;
import kg.sot.reception.dto.ConfirmRequest;
import kg.sot.reception.dto.ControlLogEntry;
import kg.sot.reception.dto.HistoryItem;
import kg.sot.reception.dto.LatestNotification;
import kg.sot.reception.dto.PatchAppointmentRequest;
import kg.sot.reception.dto.PublicAppointment;
import kg.sot.reception.dto.PublicAppointmentLookup;
import kg.sot.reception.dto.RecoverCodesResponse;
import kg.sot.reception.dto.RejectRequest;
import kg.sot.reception.dto.RescheduleRequest;
import kg.sot.reception.dto.SetStatusRequest;
import kg.sot.reception.exception.ApiException;
import kg.sot.reception.model.AppealCard;
import kg.sot.reception.model.AppealStage;
import kg.sot.reception.model.Appointment;
import kg.sot.reception.model.AppointmentStatus;
import kg.sot.reception.model.StaffUser;
import kg.sot.reception.repository.AppealCardRepository;
import kg.sot.reception.repository.AppointmentRepository;
import kg.sot.reception.util.IdGenerator;
import kg.sot.reception.util.Mappers;
import kg.sot.reception.util.PhoneUtil;
import kg.sot.reception.util.RateLimiter;
import kg.sot.reception.util.SlotCalculator;
import kg.sot.reception.util.TargetWindowResolver;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class AppointmentService {

    private static final int MAX_PIN_ATTEMPTS = 5;
    private static final Duration PIN_WINDOW = Duration.ofMinutes(15);

    private final AppointmentRepository appointmentRepository;
    private final AppealCardRepository appealCardRepository;
    private final CmsService cmsService;
    private final SlotService slotService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiter rateLimiter;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            AppealCardRepository appealCardRepository,
            CmsService cmsService,
            SlotService slotService,
            NotificationService notificationService,
            PasswordEncoder passwordEncoder,
            RateLimiter rateLimiter
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appealCardRepository = appealCardRepository;
        this.cmsService = cmsService;
        this.slotService = slotService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
    }

    // ---------------------------------------------------------------- public

    @Transactional
    public BookAppointmentResponse book(BookAppointmentRequest req) {
        String fullName = req.fullName() == null ? "" : req.fullName().trim();
        if (fullName.split("\\s+").length < 2) {
            throw ApiException.validation("Укажите полное ФИО (фамилия, имя, отчество)");
        }
        if (req.topic() == null || req.topic().isBlank()) {
            throw ApiException.validation("Укажите тему приёма");
        }
        if (req.phone() == null || req.phone().isBlank()) {
            throw ApiException.validation("Укажите телефон");
        }
        if (req.date() == null || req.slotStart() == null || req.slotStart().isBlank()) {
            throw ApiException.validation("Выберите дату и время");
        }
        if (req.targetId() == null || req.targetId().isBlank()) {
            throw ApiException.validation("Укажите, к кому запись");
        }
        List<Companion> companions = req.companions() == null ? List.of() : req.companions().stream()
                .filter(c -> c.fullName() != null && !c.fullName().isBlank())
                .map(c -> new Companion(c.fullName().trim(), blankToNull(c.phone())))
                .toList();
        if (companions.size() > 2) {
            throw ApiException.validation("Допускается не более двух сопровождающих.");
        }
        if (!slotService.isSlotFree(req.date(), req.targetId(), req.slotStart(), null)) {
            throw ApiException.slotUnavailable("Выбранный слот недоступен. Выберите другое время.");
        }

        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        String code = generateUniqueCode();
        String pin = IdGenerator.pin();
        String phone = req.phone().trim();
        String email = blankToNull(req.email());
        Map<String, Object> site = cmsService.getSiteContent();
        String who = TargetWindowResolver.shortLabel(req.targetId(), site);
        String when = SlotCalculator.formatDateRu(req.date()) + " " + req.slotStart() + "–" + req.slotEnd();

        Appointment appointment = Appointment.builder()
                .id(IdGenerator.next("apt"))
                .code(code)
                .fullName(fullName)
                .phone(phone)
                .email(email)
                .pinHash(passwordEncoder.encode(pin))
                .topic(req.topic().trim())
                .category(req.category())
                .description(blankToNull(req.description()))
                .date(req.date())
                .slotStart(req.slotStart())
                .slotEnd(req.slotEnd())
                .status(AppointmentStatus.pending_review)
                .targetId(req.targetId())
                .companions(companions)
                .history(List.of(new HistoryItem(now, "Заявка подана",
                        who + ", " + when + ". Ожидает решения приёмной.")))
                .createdAt(now)
                .updatedAt(now)
                .build();
        appointment = appointmentRepository.save(appointment);

        List<AppealCard> previous = appealCardRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(a -> PhoneUtil.matchCitizen(a.getFullName(), a.getPhone(), fullName, phone))
                .toList();
        String previousNotes = previous.isEmpty()
                ? "Предыдущих обращений не обнаружено."
                : "Найдено предыдущих обращений: " + previous.size() + ". Коды: "
                        + previous.stream().map(AppealCard::getCode).reduce((a, b) -> a + ", " + b).orElse("") + ".";

        AppealCard appeal = AppealCard.builder()
                .id(IdGenerator.next("apl"))
                .appointmentId(appointment.getId())
                .code(code)
                .fullName(fullName)
                .phone(phone)
                .email(email)
                .topic(req.topic().trim())
                .category(req.category())
                .summary(Objects.requireNonNullElse(blankToNull(req.description()), req.topic().trim()))
                .stage(AppealStage.registered)
                .previousAppealIds(previous.stream().map(AppealCard::getId).toList())
                .previousNotes(previousNotes)
                .prepNotes("")
                .controlLog(List.of())
                .notifications(List.of(notificationService.create(email, "Заявка принята на проверку",
                        "Заявка " + code + " принята общественной приёмной. " + who + ", " + when
                                + ". Запись вступает в силу после подтверждения. Статус можно проверить на сайте по коду записи.")))
                .createdAt(now)
                .updatedAt(now)
                .build();
        appealCardRepository.save(appeal);

        return new BookAppointmentResponse(code, pin, Mappers.toPublicAppointment(appointment));
    }

    @Transactional(readOnly = true)
    public PublicAppointmentLookup lookup(String code) {
        Appointment appointment = findByCode(code);
        Optional<AppealCard> appeal = appealCardRepository.findByAppointmentId(appointment.getId());
        LatestNotification latest = appeal
                .flatMap(a -> a.getNotifications().stream().findFirst())
                .map(n -> new LatestNotification(n.title(), n.body()))
                .orElse(null);
        return new PublicAppointmentLookup(
                Mappers.toPublicAppointment(appointment),
                appeal.map(AppealCard::getStage).orElse(null),
                appeal.map(AppealCard::getFeedback).orElse(null),
                latest
        );
    }

    @Transactional
    public PublicAppointment unlock(String code, String pin) {
        Appointment appointment = findByCodeAndPin(code, pin);
        return Mappers.toPublicAppointment(appointment);
    }

    @Transactional
    public PublicAppointment citizenAction(String code, CitizenActionRequest req) {
        Appointment appointment = findByCodeAndPin(code, req.pin());
        return switch (req.action()) {
            case "cancel" -> citizenCancel(appointment);
            case "reschedule" -> citizenReschedule(appointment, req);
            default -> throw ApiException.validation("Неизвестное действие: " + req.action());
        };
    }

    private PublicAppointment citizenCancel(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.cancelled) {
            throw ApiException.notCancellable("Запись уже отменена.");
        }
        if (appointment.getStatus() == AppointmentStatus.completed) {
            throw ApiException.notCancellable("Приём уже проведён — отмена невозможна.");
        }
        if (appointment.getStatus() == AppointmentStatus.rejected) {
            throw ApiException.notCancellable("Эта заявка уже не подтверждена.");
        }
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        appointment.setStatus(AppointmentStatus.cancelled);
        appointment.setUpdatedAt(now);
        appointment.addHistory(new HistoryItem(now, "Запись отменена гражданином", null));
        appointmentRepository.save(appointment);

        appealCardRepository.findByAppointmentId(appointment.getId()).ifPresent(appeal -> {
            appeal.setStage(AppealStage.cancelled);
            appeal.setUpdatedAt(now);
            appeal.addNotification(notificationService.create(null, "Запись отменена",
                    "Запись " + appeal.getCode() + " отменена. Вы можете записаться повторно на свободное время."));
            appealCardRepository.save(appeal);
        });
        return Mappers.toPublicAppointment(appointment);
    }

    private PublicAppointment citizenReschedule(Appointment appointment, CitizenActionRequest req) {
        if (appointment.getStatus() == AppointmentStatus.cancelled) {
            throw ApiException.notCancellable("Отменённую запись нельзя перенести. Создайте новую.");
        }
        if (appointment.getStatus() == AppointmentStatus.completed) {
            throw ApiException.notCancellable("Приём уже проведён.");
        }
        if (appointment.getStatus() == AppointmentStatus.rejected) {
            throw ApiException.notCancellable("Неподтверждённую заявку перенести нельзя.");
        }
        if (appointment.getStatus() == AppointmentStatus.pending_review) {
            throw ApiException.notCancellable("Дождитесь решения приёмной — затем можно перенести запись.");
        }
        if (req.date() == null || req.slotStart() == null || req.slotStart().isBlank()) {
            throw ApiException.validation("Укажите дату и время");
        }
        if (!slotService.isSlotFree(req.date(), appointment.getTargetId(), req.slotStart(), appointment.getId())) {
            throw ApiException.slotUnavailable("Выбранный слот недоступен.");
        }

        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        appointment.setDate(req.date());
        appointment.setSlotStart(req.slotStart());
        appointment.setSlotEnd(req.slotEnd());
        appointment.setStatus(AppointmentStatus.rescheduled);
        appointment.setUpdatedAt(now);
        appointment.addHistory(new HistoryItem(now, "Запись перенесена",
                req.date() + " " + req.slotStart() + "–" + req.slotEnd()));
        appointmentRepository.save(appointment);

        appealCardRepository.findByAppointmentId(appointment.getId()).ifPresent(appeal -> {
            appeal.setUpdatedAt(now);
            appeal.addNotification(notificationService.create(appointment.getEmail(), "Запись перенесена",
                    "Новая дата приёма: " + req.date() + ", " + req.slotStart() + "–" + req.slotEnd()
                            + ". Код: " + appeal.getCode() + "."));
            appealCardRepository.save(appeal);
        });
        return Mappers.toPublicAppointment(appointment);
    }

    @Transactional
    public RecoverCodesResponse recoverCodes(String phone) {
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        if (digits.length() < 9) {
            return new RecoverCodesResponse(List.of());
        }
        String last9 = digits.substring(digits.length() - 9);
        List<String> codes = appointmentRepository.findAll().stream()
                .filter(a -> {
                    String p = a.getPhone().replaceAll("\\D", "");
                    return p.endsWith(last9) || p.equals(digits);
                })
                .filter(a -> a.getStatus() != AppointmentStatus.cancelled && a.getStatus() != AppointmentStatus.rejected)
                .map(Appointment::getCode)
                .toList();
        return new RecoverCodesResponse(codes);
    }

    // ---------------------------------------------------------------- staff

    @Transactional(readOnly = true)
    public List<PublicAppointment> listStaff(AppointmentStatus status, LocalDate date, String targetId) {
        return appointmentRepository.search(status, date, targetId).stream()
                .map(Mappers::toPublicAppointment)
                .toList();
    }

    @Transactional
    public PublicAppointment confirm(String id, ConfirmRequest req, StaffUser staff) {
        Appointment appointment = findById(id);
        if (appointment.getStatus() != AppointmentStatus.pending_review) {
            throw ApiException.validation("Подтвердить можно только заявку на проверке");
        }
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        String note = blankToNull(req == null ? null : req.note());
        if (note != null) appointment.setReviewNote(note);
        appointment.setStatus(AppointmentStatus.confirmed);
        appointment.setUpdatedAt(now);
        appointment.addHistory(new HistoryItem(now, "Заявка подтверждена приёмной",
                note != null ? note : staff.getFullName()));
        appointmentRepository.save(appointment);

        Map<String, Object> site = cmsService.getSiteContent();
        String who = TargetWindowResolver.shortLabel(appointment.getTargetId(), site);
        String when = SlotCalculator.formatDateRu(appointment.getDate()) + " " + appointment.getSlotStart()
                + "–" + appointment.getSlotEnd();
        appealCardRepository.findByAppointmentId(id).ifPresent(appeal -> {
            appeal.setUpdatedAt(now);
            appeal.addNotification(notificationService.create(appointment.getEmail(), "Запись подтверждена",
                    "Запись " + appeal.getCode() + " подтверждена. " + who + ", " + when
                            + ". Явка — кабинет № 111, документ, удостоверяющий личность."));
            appealCardRepository.save(appeal);
        });
        return Mappers.toPublicAppointment(appointment);
    }

    @Transactional
    public PublicAppointment reject(String id, RejectRequest req, StaffUser staff) {
        Appointment appointment = findById(id);
        if (appointment.getStatus() != AppointmentStatus.pending_review) {
            throw ApiException.validation("Отклонить можно только заявку на проверке");
        }
        String reason = req.reason() == null ? "" : req.reason().trim();
        if (reason.length() < 8) {
            throw ApiException.validation("Укажите причину отказа (кратко, официально).");
        }
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        appointment.setStatus(AppointmentStatus.rejected);
        appointment.setReviewNote(reason);
        appointment.setUpdatedAt(now);
        appointment.addHistory(new HistoryItem(now, "Заявка не подтверждена", staff.getFullName() + ": " + reason));
        appointmentRepository.save(appointment);

        appealCardRepository.findByAppointmentId(id).ifPresent(appeal -> {
            appeal.setStage(AppealStage.cancelled);
            appeal.setUpdatedAt(now);
            appeal.addNotification(notificationService.create(appointment.getEmail(), "Запись не подтверждена",
                    "По заявке " + appeal.getCode() + " запись не подтверждена. " + reason));
            appealCardRepository.save(appeal);
        });
        return Mappers.toPublicAppointment(appointment);
    }

    @Transactional
    public PublicAppointment staffCancel(String id, CancelRequest req, StaffUser staff) {
        Appointment appointment = findById(id);
        if (appointment.getStatus() == AppointmentStatus.cancelled) {
            throw ApiException.notCancellable("Уже отменена");
        }
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        String detail = blankToNull(req == null ? null : req.reason());
        if (detail == null) detail = "Отменил(а): " + staff.getFullName();
        appointment.setStatus(AppointmentStatus.cancelled);
        appointment.setUpdatedAt(now);
        appointment.addHistory(new HistoryItem(now, "Запись отменена сотрудником", detail));
        appointmentRepository.save(appointment);

        String finalDetail = detail;
        appealCardRepository.findByAppointmentId(id).ifPresent(appeal -> {
            appeal.setStage(AppealStage.cancelled);
            appeal.setUpdatedAt(now);
            appeal.addControlLog(new ControlLogEntry(
                    IdGenerator.next("cl"), now, staff.getId(), staff.getFullName(), "Отмена записи", finalDetail));
            appeal.addNotification(notificationService.create(appointment.getEmail(), "Запись отменена приёмной",
                    "Запись " + appeal.getCode() + " отменена сотрудником. " + finalDetail));
            appealCardRepository.save(appeal);
        });
        return Mappers.toPublicAppointment(appointment);
    }

    @Transactional
    public PublicAppointment staffRestore(String id, StaffUser staff) {
        Appointment appointment = findById(id);
        if (appointment.getStatus() != AppointmentStatus.cancelled && appointment.getStatus() != AppointmentStatus.no_show) {
            throw ApiException.validation("Вернуть можно только отменённую запись или неявку");
        }
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        appointment.setStatus(AppointmentStatus.confirmed);
        appointment.setUpdatedAt(now);
        appointment.addHistory(new HistoryItem(now, "Запись восстановлена", staff.getFullName()));
        appointmentRepository.save(appointment);

        appealCardRepository.findByAppointmentId(id).ifPresent(appeal -> {
            appeal.setStage(AppealStage.registered);
            appeal.setUpdatedAt(now);
            appeal.addControlLog(new ControlLogEntry(
                    IdGenerator.next("cl"), now, staff.getId(), staff.getFullName(),
                    "Восстановление записи", "Возврат в очередь / ожидание"));
            appeal.addNotification(notificationService.create(appointment.getEmail(), "Запись восстановлена",
                    "Запись " + appeal.getCode() + " снова активна. Дата: " + appointment.getDate()
                            + " " + appointment.getSlotStart() + "."));
            appealCardRepository.save(appeal);
        });
        return Mappers.toPublicAppointment(appointment);
    }

    @Transactional
    public PublicAppointment patch(String id, PatchAppointmentRequest req, StaffUser staff) {
        Appointment appointment = findById(id);
        String fullName = req.fullName() != null ? req.fullName().trim() : appointment.getFullName();
        String phone = req.phone() != null ? req.phone().trim() : appointment.getPhone();
        String email = req.email() != null ? blankToNull(req.email()) : appointment.getEmail();
        String topic = req.topic() != null ? req.topic().trim() : appointment.getTopic();
        var category = req.category() != null ? req.category() : appointment.getCategory();
        String description = req.description() != null ? blankToNull(req.description()) : appointment.getDescription();

        if (fullName.isBlank() || fullName.split("\\s+").length < 2) {
            throw ApiException.validation("Укажите полное ФИО");
        }
        if (phone.isBlank()) throw ApiException.validation("Укажите телефон");
        if (topic.isBlank()) throw ApiException.validation("Укажите тему");

        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        appointment.setFullName(fullName);
        appointment.setPhone(phone);
        appointment.setEmail(email);
        appointment.setTopic(topic);
        appointment.setCategory(category);
        appointment.setDescription(description);
        appointment.setUpdatedAt(now);
        appointment.addHistory(new HistoryItem(now, "Данные записи изменены", staff.getFullName()));
        appointmentRepository.save(appointment);

        String finalFullName = fullName;
        String finalPhone = phone;
        String finalEmail = email;
        String finalTopic = topic;
        var finalCategory = category;
        String finalDescription = description;
        appealCardRepository.findByAppointmentId(id).ifPresent(appeal -> {
            appeal.setFullName(finalFullName);
            appeal.setPhone(finalPhone);
            appeal.setEmail(finalEmail);
            appeal.setTopic(finalTopic);
            appeal.setCategory(finalCategory);
            appeal.setSummary(Objects.requireNonNullElse(finalDescription,
                    appeal.getSummary() != null && !appeal.getSummary().isBlank() ? appeal.getSummary() : finalTopic));
            appeal.setUpdatedAt(now);
            appealCardRepository.save(appeal);
        });
        return Mappers.toPublicAppointment(appointment);
    }

    @Transactional
    public PublicAppointment setStatus(String id, SetStatusRequest req, StaffUser staff) {
        Appointment appointment = findById(id);
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        appointment.setStatus(req.status());
        appointment.setUpdatedAt(now);
        appointment.addHistory(new HistoryItem(now, "Статус записи: " + req.status(),
                blankToNull(req.note()) != null ? req.note() : staff.getFullName()));
        appointmentRepository.save(appointment);

        AppealStage nextStage = mapAppointmentStatusToStage(req.status());
        appealCardRepository.findByAppointmentId(id).ifPresent(appeal -> {
            if (nextStage != null) appeal.setStage(nextStage);
            appeal.setUpdatedAt(now);
            appeal.addControlLog(new ControlLogEntry(
                    IdGenerator.next("cl"), now, staff.getId(), staff.getFullName(),
                    "Статус записи → " + req.status(),
                    blankToNull(req.note()) != null ? req.note() : "Смена статуса сотрудником"));
            appealCardRepository.save(appeal);
        });
        return Mappers.toPublicAppointment(appointment);
    }

    @Transactional
    public PublicAppointment staffReschedule(String id, RescheduleRequest req, StaffUser staff) {
        Appointment appointment = findById(id);
        if (appointment.getStatus() == AppointmentStatus.cancelled) {
            throw ApiException.validation("Сначала восстановите отменённую запись");
        }
        if (appointment.getStatus() == AppointmentStatus.completed) {
            throw ApiException.validation("Приём уже проведён");
        }
        if (!slotService.isSlotFree(req.date(), appointment.getTargetId(), req.slotStart(), appointment.getId())) {
            throw ApiException.slotUnavailable("Выбранный слот недоступен.");
        }

        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        appointment.setDate(req.date());
        appointment.setSlotStart(req.slotStart());
        appointment.setSlotEnd(req.slotEnd());
        appointment.setStatus(AppointmentStatus.rescheduled);
        appointment.setUpdatedAt(now);
        appointment.addHistory(new HistoryItem(now, "Перенос сотрудником",
                req.date() + " " + req.slotStart() + "–" + req.slotEnd() + " · " + staff.getFullName()));
        appointmentRepository.save(appointment);

        appealCardRepository.findByAppointmentId(id).ifPresent(appeal -> {
            appeal.setUpdatedAt(now);
            appeal.addNotification(notificationService.create(appointment.getEmail(), "Запись перенесена",
                    "Новая дата: " + req.date() + ", " + req.slotStart() + "–" + req.slotEnd()
                            + ". Код: " + appeal.getCode() + "."));
            appealCardRepository.save(appeal);
        });
        return Mappers.toPublicAppointment(appointment);
    }

    // ---------------------------------------------------------------- helpers

    private AppealStage mapAppointmentStatusToStage(AppointmentStatus status) {
        return switch (status) {
            case cancelled, rejected, no_show -> AppealStage.cancelled;
            case completed -> AppealStage.reception_done;
            case confirmed, pending_review, rescheduled -> AppealStage.registered;
        };
    }

    Appointment findById(String id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Запись не найдена"));
    }

    Appointment findByCode(String code) {
        return appointmentRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> ApiException.notFound("Запись не найдена"));
    }

    private Appointment findByCodeAndPin(String code, String pin) {
        String rateLimitKey = "pin:" + code.trim().toUpperCase();
        if (!rateLimiter.tryConsume(rateLimitKey, MAX_PIN_ATTEMPTS, PIN_WINDOW)) {
            throw ApiException.rateLimited("Слишком много попыток. Повторите позже.");
        }
        Appointment appointment = findByCode(code);
        if (pin == null || !passwordEncoder.matches(pin.trim(), appointment.getPinHash())) {
            throw ApiException.invalidPin("Запись не найдена. Проверьте код и PIN.");
        }
        rateLimiter.reset(rateLimitKey);
        return appointment;
    }

    private String generateUniqueCode() {
        int year = Year.now(SlotService.BISHKEK).getValue();
        for (int i = 0; i < 30; i++) {
            String code = IdGenerator.appointmentCode(year);
            if (!appointmentRepository.existsByCodeIgnoreCase(code)) return code;
        }
        throw new IllegalStateException("Не удалось сгенерировать уникальный код записи");
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
