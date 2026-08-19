package kg.sot.reception.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import kg.sot.reception.config.SeedProperties;
import kg.sot.reception.dto.EligibilityNode;
import kg.sot.reception.dto.SurveyBundle;
import kg.sot.reception.model.CalendarSettings;
import kg.sot.reception.model.EligibilityTree;
import kg.sot.reception.model.SiteContent;
import kg.sot.reception.model.SurveyConfig;
import kg.sot.reception.repository.AppealCardRepository;
import kg.sot.reception.repository.AppointmentRepository;
import kg.sot.reception.repository.CalendarSettingsRepository;
import kg.sot.reception.repository.EligibilityTreeRepository;
import kg.sot.reception.repository.SiteContentRepository;
import kg.sot.reception.repository.StaffUserRepository;
import kg.sot.reception.repository.SurveyConfigRepository;
import kg.sot.reception.util.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Первичное наполнение БД: тексты сайта / дерево допуска / опросник / график — всегда
 * (без них бэкенд не сможет отдать /public/bootstrap). Демо-сотрудники и демо-заявки —
 * только при app.seed.demo-data=true (по умолчанию включено для среды разработки).
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CalendarSettingsRepository calendarSettingsRepository;
    private final SiteContentRepository siteContentRepository;
    private final EligibilityTreeRepository eligibilityTreeRepository;
    private final SurveyConfigRepository surveyConfigRepository;
    private final StaffUserRepository staffUserRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppealCardRepository appealCardRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties seedProperties;
    private final DemoDataSeeder demoDataSeeder;

    public DataSeeder(
            CalendarSettingsRepository calendarSettingsRepository,
            SiteContentRepository siteContentRepository,
            EligibilityTreeRepository eligibilityTreeRepository,
            SurveyConfigRepository surveyConfigRepository,
            StaffUserRepository staffUserRepository,
            AppointmentRepository appointmentRepository,
            AppealCardRepository appealCardRepository,
            PasswordEncoder passwordEncoder,
            SeedProperties seedProperties,
            DemoDataSeeder demoDataSeeder
    ) {
        this.calendarSettingsRepository = calendarSettingsRepository;
        this.siteContentRepository = siteContentRepository;
        this.eligibilityTreeRepository = eligibilityTreeRepository;
        this.surveyConfigRepository = surveyConfigRepository;
        this.staffUserRepository = staffUserRepository;
        this.appointmentRepository = appointmentRepository;
        this.appealCardRepository = appealCardRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedProperties = seedProperties;
        this.demoDataSeeder = demoDataSeeder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        seedCalendar();
        seedSiteContent();
        seedEligibilityTree();
        seedSurveyConfig();

        if (seedProperties.demoData()) {
            if (staffUserRepository.count() == 0) {
                demoDataSeeder.seedStaff(passwordEncoder);
                log.warn("Созданы демо-учётки сотрудников (логины/пароли — см. README). "
                        + "Для боевого контура отключите APP_SEED_DEMO_DATA и заведите реальных пользователей.");
            }
            if (appointmentRepository.count() == 0) {
                demoDataSeeder.seedAppointmentsAndAppeals(passwordEncoder);
            }
        }
    }

    private void seedCalendar() {
        if (calendarSettingsRepository.count() > 0) return;
        Map<String, Object> rules = readJson("seed/calendar-rules.json", new TypeReference<>() {
        });
        CalendarSettings settings = CalendarSettings.builder()
                .id(CalendarSettings.SINGLETON_ID)
                .receptionWeekdays(List.of(2, 4))
                .dayStartMinutes(8 * 60)
                .dayEndMinutes(12 * 60)
                .slotDurationMinutes(20)
                .breakMinutes(5)
                .bookingHorizonDays(45)
                .closedDates(List.of())
                .extraOpenDates(List.of())
                .rulesText(String.valueOf(rules.get("rulesText")))
                .updatedAt(OffsetDateTime.now())
                .build();
        calendarSettingsRepository.save(settings);
        log.info("Сид: настройки календаря созданы");
    }

    private void seedSiteContent() {
        if (siteContentRepository.count() > 0) return;
        Map<String, Object> site = readJson("seed/site.json", new TypeReference<>() {
        });
        SiteContent entity = SiteContent.builder()
                .id(SiteContent.SINGLETON_ID)
                .content(new LinkedHashMap<>(site))
                .updatedAt(OffsetDateTime.now())
                .build();
        siteContentRepository.save(entity);
        log.info("Сид: тексты сайта загружены из content/site.json");
    }

    private void seedEligibilityTree() {
        if (eligibilityTreeRepository.count() > 0) return;
        List<EligibilityNode> nodes = readJson("seed/eligibility-tree.json", new TypeReference<>() {
        });
        EligibilityTree entity = EligibilityTree.builder()
                .id(EligibilityTree.SINGLETON_ID)
                .nodes(nodes)
                .updatedAt(OffsetDateTime.now())
                .build();
        eligibilityTreeRepository.save(entity);
        log.info("Сид: дерево допуска загружено из content/eligibility-tree.json");
    }

    private void seedSurveyConfig() {
        if (surveyConfigRepository.count() > 0) return;
        SurveyBundle bundle = readJson("seed/survey.json", new TypeReference<>() {
        });
        SurveyConfig entity = SurveyConfig.builder()
                .id(SurveyConfig.SINGLETON_ID)
                .meta(bundle.meta())
                .questions(bundle.questions())
                .updatedAt(OffsetDateTime.now())
                .build();
        surveyConfigRepository.save(entity);
        log.info("Сид: опросник загружен из content/survey.json");
    }

    private <T> T readJson(String classpathLocation, TypeReference<T> type) {
        try (InputStream in = new ClassPathResource(classpathLocation).getInputStream()) {
            return JsonMapper.INSTANCE.readValue(in, type);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось загрузить сид " + classpathLocation, e);
        }
    }
}
