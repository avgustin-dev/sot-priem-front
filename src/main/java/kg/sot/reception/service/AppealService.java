package kg.sot.reception.service;

import kg.sot.reception.dto.AddControlLogRequest;
import kg.sot.reception.dto.AppealCardDto;
import kg.sot.reception.dto.Assignment;
import kg.sot.reception.dto.CompletePrepRequest;
import kg.sot.reception.dto.CompleteReceptionRequest;
import kg.sot.reception.dto.ControlLogEntry;
import kg.sot.reception.dto.Feedback;
import kg.sot.reception.dto.FeedbackRequest;
import kg.sot.reception.dto.HistoryItem;
import kg.sot.reception.dto.ReceptionProtocol;
import kg.sot.reception.dto.SetAppealStageRequest;
import kg.sot.reception.dto.SetAssignmentStatusRequest;
import kg.sot.reception.dto.SubmitFinalAnswerRequest;
import kg.sot.reception.exception.ApiException;
import kg.sot.reception.model.AppealCard;
import kg.sot.reception.model.AppealStage;
import kg.sot.reception.model.AppointmentStatus;
import kg.sot.reception.model.AssignmentStatus;
import kg.sot.reception.model.StaffUser;
import kg.sot.reception.repository.AppealCardRepository;
import kg.sot.reception.repository.AppointmentRepository;
import kg.sot.reception.util.IdGenerator;
import kg.sot.reception.util.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AppealService {

    private final AppealCardRepository appealCardRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    public AppealService(
            AppealCardRepository appealCardRepository,
            AppointmentRepository appointmentRepository,
            NotificationService notificationService
    ) {
        this.appealCardRepository = appealCardRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<AppealCardDto> list(AppealStage stage) {
        List<AppealCard> cards = stage != null
                ? appealCardRepository.findByStageOrderByCreatedAtDesc(stage)
                : appealCardRepository.findAllByOrderByCreatedAtDesc();
        return cards.stream().map(Mappers::toAppealCardDto).toList();
    }

    @Transactional(readOnly = true)
    public AppealCardDto get(String id) {
        return Mappers.toAppealCardDto(findById(id));
    }

    @Transactional
    public AppealCardDto completePrep(String id, CompletePrepRequest req, StaffUser staff) {
        AppealCard appeal = findById(id);
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        appeal.setSummary(req.summary());
        appeal.setPrepNotes(req.prepNotes());
        appeal.setCategory(req.category());
        appeal.setStage(AppealStage.ready_for_reception);
        appeal.setPrepCompletedBy(staff.getFullName());
        appeal.setPrepCompletedAt(now);
        appeal.setUpdatedAt(now);
        return Mappers.toAppealCardDto(appealCardRepository.save(appeal));
    }

    @Transactional
    public AppealCardDto markReady(String id) {
        AppealCard appeal = findById(id);
        appeal.setStage(AppealStage.ready_for_reception);
        appeal.setUpdatedAt(OffsetDateTime.now(SlotService.BISHKEK));
        return Mappers.toAppealCardDto(appealCardRepository.save(appeal));
    }

    @Transactional
    public AppealCardDto completeReception(String id, CompleteReceptionRequest req, StaffUser staff) {
        AppealCard appeal = findById(id);
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);

        ReceptionProtocol protocol = new ReceptionProtocol(
                now, staff.getFullName(), req.citizenStatement(), req.leadershipExplanation(),
                req.assignmentText(), req.responsibleUserId(), req.responsibleName(),
                req.specialistsInvolved(), req.notes());
        appeal.setReceptionProtocol(protocol);
        appeal.setAssignment(new Assignment(
                req.assignmentText(), req.responsibleUserId(), req.responsibleName(),
                now.toLocalDate().plusDays(14), AssignmentStatus.open, now));
        appeal.setStage(AppealStage.in_control);
        appeal.addControlLog(new ControlLogEntry(
                IdGenerator.next("cl"), now, staff.getId(), staff.getFullName(),
                "Поручение выдано", req.assignmentText()));
        appeal.addNotification(notificationService.create(appeal.getEmail(), "Приём проведён",
                "По итогам приёма выдано поручение. Ответственный приступит к исполнению."));
        appeal.setUpdatedAt(now);
        appealCardRepository.save(appeal);

        appointmentRepository.findById(appeal.getAppointmentId()).ifPresent(apt -> {
            apt.setStatus(AppointmentStatus.completed);
            apt.setUpdatedAt(now);
            apt.addHistory(new HistoryItem(now, "Личный приём проведён", null));
            appointmentRepository.save(apt);
        });

        return Mappers.toAppealCardDto(appeal);
    }

    @Transactional
    public AppealCardDto addControlLog(String id, AddControlLogRequest req, StaffUser staff) {
        AppealCard appeal = findById(id);
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        appeal.addControlLog(new ControlLogEntry(
                IdGenerator.next("cl"), now, staff.getId(), staff.getFullName(), req.action(), req.comment()));
        if (appeal.getAssignment() != null && appeal.getAssignment().status() == AssignmentStatus.open) {
            Assignment a = appeal.getAssignment();
            appeal.setAssignment(new Assignment(a.text(), a.responsibleUserId(), a.responsibleName(),
                    a.dueDate(), AssignmentStatus.in_progress, a.createdAt()));
        }
        appeal.setUpdatedAt(now);
        return Mappers.toAppealCardDto(appealCardRepository.save(appeal));
    }

    @Transactional
    public AppealCardDto setAssignmentStatus(String id, SetAssignmentStatusRequest req) {
        AppealCard appeal = findById(id);
        if (appeal.getAssignment() == null) {
            throw ApiException.validation("У обращения нет поручения");
        }
        Assignment a = appeal.getAssignment();
        appeal.setAssignment(new Assignment(a.text(), a.responsibleUserId(), a.responsibleName(),
                a.dueDate(), req.status(), a.createdAt()));
        appeal.setUpdatedAt(OffsetDateTime.now(SlotService.BISHKEK));
        return Mappers.toAppealCardDto(appealCardRepository.save(appeal));
    }

    @Transactional
    public AppealCardDto submitFinalAnswer(String id, SubmitFinalAnswerRequest req, StaffUser staff) {
        AppealCard appeal = findById(id);
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        appeal.setFinalAnswer(req.answer());
        appeal.setFinalAnswerAt(now);
        appeal.setStage(AppealStage.answered);
        if (appeal.getAssignment() != null) {
            Assignment a = appeal.getAssignment();
            appeal.setAssignment(new Assignment(a.text(), a.responsibleUserId(), a.responsibleName(),
                    a.dueDate(), AssignmentStatus.done, a.createdAt()));
        }
        appeal.addControlLog(new ControlLogEntry(
                IdGenerator.next("cl"), now, staff.getId(), staff.getFullName(),
                "Ответ направлен гражданину", req.answer().length() > 200 ? req.answer().substring(0, 200) : req.answer()));
        appeal.addNotification(notificationService.create(appeal.getEmail(), "Ответ по обращению готов",
                "По обращению " + appeal.getCode() + " подготовлен ответ. Оцените работу: /service-evaluation/" + appeal.getCode()));
        appeal.setUpdatedAt(now);
        return Mappers.toAppealCardDto(appealCardRepository.save(appeal));
    }

    @Transactional
    public AppealCardDto setStage(String id, SetAppealStageRequest req, StaffUser staff) {
        AppealCard appeal = findById(id);
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        appeal.setStage(req.stage());
        appeal.addControlLog(new ControlLogEntry(
                IdGenerator.next("cl"), now, staff.getId(), staff.getFullName(),
                "Этап → " + req.stage(), req.note() != null && !req.note().isBlank() ? req.note() : "Смена этапа сотрудником"));
        appeal.setUpdatedAt(now);
        appealCardRepository.save(appeal);

        AppointmentStatus nextStatus = mapStageToAppointmentStatus(req.stage());
        if (nextStatus != null) {
            appointmentRepository.findById(appeal.getAppointmentId()).ifPresent(apt -> {
                apt.setStatus(nextStatus);
                apt.setUpdatedAt(now);
                apt.addHistory(new HistoryItem(now, "Этап обращения: " + req.stage(), staff.getFullName()));
                appointmentRepository.save(apt);
            });
        }
        return Mappers.toAppealCardDto(appeal);
    }

    @Transactional
    public void submitFeedback(String code, FeedbackRequest req) {
        AppealCard appeal = appealCardRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> ApiException.notFound("Обращение не найдено"));
        if (appeal.getStage() == AppealStage.cancelled) {
            throw ApiException.notCancellable("По отменённой записи оценка не принимается");
        }
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        boolean isEdit = appeal.getFeedback() != null;
        appeal.setFeedback(new Feedback(req.respectful(), req.clearNextSteps(), req.convenient(), req.deadlinesMet(),
                req.comment(), now));
        if (appeal.getStage() == AppealStage.answered || appeal.getStage() == AppealStage.in_control) {
            appeal.setStage(AppealStage.closed);
        }
        appeal.addControlLog(new ControlLogEntry(
                IdGenerator.next("cl"), now, "citizen", appeal.getFullName(),
                isEdit ? "Оценка приёма изменена" : "Оценка приёма направлена", "Обратная связь гражданина"));
        appeal.setUpdatedAt(now);
        appealCardRepository.save(appeal);
    }

    private AppointmentStatus mapStageToAppointmentStatus(AppealStage stage) {
        return switch (stage) {
            case cancelled -> AppointmentStatus.cancelled;
            case reception_done, closed, answered, in_control -> AppointmentStatus.completed;
            case registered, under_review, ready_for_reception -> AppointmentStatus.confirmed;
        };
    }

    private AppealCard findById(String id) {
        return appealCardRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Обращение не найдено"));
    }
}
