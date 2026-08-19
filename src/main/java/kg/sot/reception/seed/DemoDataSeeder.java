package kg.sot.reception.seed;

import kg.sot.reception.dto.Assignment;
import kg.sot.reception.dto.Companion;
import kg.sot.reception.dto.ControlLogEntry;
import kg.sot.reception.dto.Feedback;
import kg.sot.reception.dto.HistoryItem;
import kg.sot.reception.dto.NotificationItem;
import kg.sot.reception.dto.ReceptionProtocol;
import kg.sot.reception.model.AppealCard;
import kg.sot.reception.model.AppealCategory;
import kg.sot.reception.model.AppealStage;
import kg.sot.reception.model.Appointment;
import kg.sot.reception.model.AppointmentStatus;
import kg.sot.reception.model.AssignmentStatus;
import kg.sot.reception.model.NotificationChannel;
import kg.sot.reception.model.Role;
import kg.sot.reception.model.StaffUser;
import kg.sot.reception.repository.AppealCardRepository;
import kg.sot.reception.repository.AppointmentRepository;
import kg.sot.reception.repository.StaffUserRepository;
import kg.sot.reception.service.SlotService;
import kg.sot.reception.util.SlotCalculator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** Демо-данные, зеркалящие src/lib/seed.ts фронта — только для среды разработки/показа. */
@Component
public class DemoDataSeeder {

    private final StaffUserRepository staffUserRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppealCardRepository appealCardRepository;

    public DemoDataSeeder(
            StaffUserRepository staffUserRepository,
            AppointmentRepository appointmentRepository,
            AppealCardRepository appealCardRepository
    ) {
        this.staffUserRepository = staffUserRepository;
        this.appointmentRepository = appointmentRepository;
        this.appealCardRepository = appealCardRepository;
    }

    public void seedStaff(PasswordEncoder encoder) {
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        List<StaffUser> staff = List.of(
                staffUser("u-chairman", "predsedatel", encoder.encode("vs2026"),
                        "Сатыев Медербек Асанбекович", Role.leadership,
                        "Председатель Верховного суда Кыргызской Республики",
                        "Верховный суд Кыргызской Республики", now),
                staffUser("u-admin", "admin", encoder.encode("admin123"),
                        "Абдылдаев Нурлан Токтосунович", Role.admin,
                        "Администратор платформы",
                        "ИТ-служба Верховного суда Кыргызской Республики", now),
                staffUser("u-reception", "priemnaya", encoder.encode("priem123"),
                        "Касымова Айгуль Бакытовна", Role.reception,
                        "Главный специалист",
                        "Отдел по работе с гражданами", now),
                staffUser("u-leadership", "rukovodstvo", encoder.encode("sud2026"),
                        "Руководство Верховного суда КР", Role.leadership,
                        "Приём руководства",
                        "Аппарат Верховного суда КР", now),
                staffUser("u-resp-1", "otvet1", encoder.encode("otvet123"),
                        "Жумабеков Эркин Сапарович", Role.responsible,
                        "Ответственный по обращениям",
                        "Аппарат Верховного суда КР", now),
                staffUser("u-resp-2", "otvet2", encoder.encode("otvet123"),
                        "Сыдыкова Меерим Асановна", Role.responsible,
                        "Ответственный по обращениям",
                        "Отдел анализа судебной практики", now)
        );
        staffUserRepository.saveAll(staff);
    }

    public void seedAppointmentsAndAppeals(PasswordEncoder encoder) {
        OffsetDateTime now = OffsetDateTime.now(SlotService.BISHKEK);
        LocalDate today = now.toLocalDate();
        LocalDate demoDate1 = nextWeekday(today, 2);
        LocalDate demoDate2 = nextWeekday(today, 4);
        LocalDate pastDate = today.minusDays(14);
        String pinHash = encoder.encode("0000");

        Appointment apt1 = Appointment.builder()
                .id("apt-demo-1").code("VS-2026-1001")
                .fullName("Токтосунов Алмаз Бектурович").phone("+996700111001")
                .email("almaz.t@example.com").pinHash(encoder.encode("4821"))
                .topic("Улучшение информирования о сроках рассмотрения жалоб")
                .category(AppealCategory.organization)
                .description("Прошу разъяснить порядок информирования граждан о ходе рассмотрения жалоб в судах и предложить единый канал уведомлений.")
                .date(demoDate1).slotStart("15:00").slotEnd("15:20")
                .status(AppointmentStatus.confirmed).targetId("deputy_bakirova")
                .companions(List.of())
                .history(List.of(new HistoryItem(now, "Запись создана", "Онлайн-запись через платформу")))
                .createdAt(now).updatedAt(now)
                .build();

        Appointment apt2 = Appointment.builder()
                .id("apt-demo-2").code("VS-2026-1002")
                .fullName("Исакова Назгуль Асанбековна").phone("+996555222003")
                .pinHash(encoder.encode("7390"))
                .topic("Предложение по приёмным часам в региональных судах")
                .category(AppealCategory.court_activity)
                .description("Предлагаю расширить график приёма в областных судах.")
                .date(demoDate2).slotStart("09:00").slotEnd("09:20")
                .status(AppointmentStatus.confirmed).targetId("chairman")
                .companions(List.of(new Companion("Исаков Бакыт", "+996555000111")))
                .history(List.of(new HistoryItem(now, "Запись создана", null)))
                .createdAt(now).updatedAt(now)
                .build();

        Appointment aptPast = Appointment.builder()
                .id("apt-past-1").code("VS-2026-0910")
                .fullName("Токтосунов Алмаз Бектурович").phone("+996700111001")
                .pinHash(pinHash)
                .topic("Доступность информации на сайте суда")
                .category(AppealCategory.court_activity)
                .date(pastDate).slotStart("08:00").slotEnd("08:20")
                .status(AppointmentStatus.completed).targetId("reception")
                .companions(List.of())
                .history(List.of(new HistoryItem(pastDate.atStartOfDay(SlotService.BISHKEK).toOffsetDateTime(), "Приём проведён", null)))
                .createdAt(now.minusDays(20)).updatedAt(now.minusDays(10))
                .build();

        Appointment aptPending = Appointment.builder()
                .id("apt-pending-1").code("VS-2026-1003")
                .fullName("Мамытов Эрлан Сагынович").phone("+996700333221")
                .email("erlan.m@example.com").pinHash(encoder.encode("5502"))
                .topic("Предложение по информированию о графике приёма в областных судах")
                .category(AppealCategory.organization)
                .description("Прошу рассмотреть публикацию единого понятного графика приёма граждан во всех областных судах на государственном и официальном языках.")
                .date(demoDate1).slotStart("08:00").slotEnd("08:20")
                .status(AppointmentStatus.pending_review).targetId("reception")
                .companions(List.of())
                .history(List.of(new HistoryItem(now, "Заявка подана", "Ожидает решения общественной приёмной")))
                .createdAt(now).updatedAt(now)
                .build();

        appointmentRepository.saveAll(List.of(apt1, apt2, aptPast, aptPending));

        AppealCard apl1 = AppealCard.builder()
                .id("apl-demo-1").appointmentId("apt-demo-1").code("VS-2026-1001")
                .fullName("Токтосунов Алмаз Бектурович").phone("+996700111001").email("almaz.t@example.com")
                .topic("Улучшение информирования о сроках рассмотрения жалоб")
                .category(AppealCategory.organization)
                .summary("Гражданин предлагает единый канал уведомлений о ходе рассмотрения жалоб.")
                .stage(AppealStage.registered)
                .previousAppealIds(List.of("apl-past-1"))
                .previousNotes("Ранее обращался по вопросу доступности информации на сайте (VS-2026-0910).")
                .prepNotes("")
                .controlLog(List.of())
                .notifications(List.of(new NotificationItem("n1", now, NotificationChannel.system,
                        "Запись подтверждена", "Ваша запись на приём подтверждена. Код: VS-2026-1001.", false)))
                .createdAt(now).updatedAt(now)
                .build();

        AppealCard apl2 = AppealCard.builder()
                .id("apl-demo-2").appointmentId("apt-demo-2").code("VS-2026-1002")
                .fullName("Исакова Назгуль Асанбековна").phone("+996555222003")
                .topic("Предложение по приёмным часам в региональных судах")
                .category(AppealCategory.court_activity)
                .summary("Предложение расширить график приёма в областных судах.")
                .stage(AppealStage.under_review)
                .previousAppealIds(List.of())
                .previousNotes("")
                .prepNotes("Проведена предварительная беседа. Вопрос относится к организации деятельности судов. Рекомендовано подготовить справку по текущим графикам приёма.")
                .prepCompletedBy("Касымова Айгуль Бакытовна")
                .prepCompletedAt(now)
                .controlLog(List.of())
                .notifications(List.of(new NotificationItem("n2", now, NotificationChannel.system,
                        "Запись подтверждена", "Ваша запись на приём подтверждена. Код: VS-2026-1002.", true)))
                .createdAt(now).updatedAt(now)
                .build();

        OffsetDateTime heldAt = pastDate.atTime(8, 10).atZone(SlotService.BISHKEK).toOffsetDateTime();
        AppealCard aplPast = AppealCard.builder()
                .id("apl-past-1").appointmentId("apt-past-1").code("VS-2026-0910")
                .fullName("Токтосунов Алмаз Бектурович").phone("+996700111001")
                .topic("Доступность информации на сайте суда")
                .category(AppealCategory.court_activity)
                .summary("Повторное обращение того же гражданина (для проверки мониторинга).")
                .stage(AppealStage.closed)
                .previousAppealIds(List.of())
                .previousNotes("")
                .prepNotes("Вопрос изучен, даны разъяснения.")
                .prepCompletedBy("Касымова Айгуль Бакытовна")
                .prepCompletedAt(now.minusDays(15))
                .receptionProtocol(new ReceptionProtocol(
                        heldAt, "Руководство Верховного суда КР",
                        "Сложно найти сведения о графике и контактах на сайте.",
                        "Разъяснён порядок размещения информации. Поручено проработать улучшение навигации.",
                        "Подготовить предложения по доработке раздела «Гражданам» на сайте.",
                        "u-resp-1", "Жумабеков Эркин Сапарович", "Отдел информатизации", null))
                .assignment(new Assignment(
                        "Подготовить предложения по доработке раздела «Гражданам» на сайте.",
                        "u-resp-1", "Жумабеков Эркин Сапарович",
                        today.minusDays(7), AssignmentStatus.done, pastDate.atStartOfDay(SlotService.BISHKEK).toOffsetDateTime()))
                .controlLog(List.of(
                        new ControlLogEntry("cl1", now.minusDays(12), "u-resp-1", "Жумабеков Эркин Сапарович",
                                "Взято в работу", "Собран аудит структуры сайта."),
                        new ControlLogEntry("cl2", now.minusDays(10), "u-resp-1", "Жумабеков Эркин Сапарович",
                                "Ответ подготовлен", "Направлен обоснованный ответ гражданину.")
                ))
                .finalAnswer("Уважаемый Алмаз Бектурович! Ваше обращение рассмотрено. Подготовлены предложения по улучшению навигации раздела «Гражданам». О результатах реализации будет сообщено дополнительно.")
                .finalAnswerAt(now.minusDays(10))
                .feedback(new Feedback(5, 4, 4, 5, "Спасибо за внимательное отношение.", now.minusDays(9)))
                .notifications(List.of())
                .createdAt(now.minusDays(20)).updatedAt(now.minusDays(9))
                .build();

        AppealCard aplPending = AppealCard.builder()
                .id("apl-pending-1").appointmentId("apt-pending-1").code("VS-2026-1003")
                .fullName("Мамытов Эрлан Сагынович").phone("+996700333221").email("erlan.m@example.com")
                .topic("Предложение по информированию о графике приёма в областных судах")
                .category(AppealCategory.organization)
                .summary("Предложение публиковать единый график приёма граждан в областных судах на двух языках.")
                .stage(AppealStage.registered)
                .previousAppealIds(List.of())
                .previousNotes("Предыдущих обращений не обнаружено.")
                .prepNotes("")
                .controlLog(List.of())
                .notifications(List.of(new NotificationItem("n-pending-1", now, NotificationChannel.email,
                        "Заявка принята на проверку",
                        "Заявка VS-2026-1003 принята общественной приёмной. Запись вступает в силу после подтверждения. Статус можно проверить на сайте по коду записи.",
                        false)))
                .createdAt(now).updatedAt(now)
                .build();

        appealCardRepository.saveAll(List.of(apl1, apl2, aplPast, aplPending));
    }

    private StaffUser staffUser(
            String id, String login, String passwordHash, String fullName,
            Role role, String position, String department, OffsetDateTime now
    ) {
        return StaffUser.builder()
                .id(id).login(login).passwordHash(passwordHash).fullName(fullName)
                .role(role).position(position).department(department)
                .createdAt(now).updatedAt(now)
                .build();
    }

    /** Ближайший день недели (JS: 0=вс…6=сб) строго после today, как nextWeekday() в seed.ts. */
    private LocalDate nextWeekday(LocalDate today, int targetJsDow) {
        int currentJsDow = SlotCalculator.jsWeekday(today);
        int add = ((targetJsDow - currentJsDow) % 7 + 7) % 7;
        if (add == 0) add = 7;
        return today.plusDays(add);
    }
}
