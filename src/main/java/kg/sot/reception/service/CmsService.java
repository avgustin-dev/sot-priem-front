package kg.sot.reception.service;

import kg.sot.reception.dto.CalendarSettingsDto;
import kg.sot.reception.dto.EligibilityNode;
import kg.sot.reception.dto.PublicBootstrap;
import kg.sot.reception.dto.SurveyBundle;
import kg.sot.reception.model.CalendarSettings;
import kg.sot.reception.model.EligibilityTree;
import kg.sot.reception.model.SiteContent;
import kg.sot.reception.model.SurveyConfig;
import kg.sot.reception.repository.CalendarSettingsRepository;
import kg.sot.reception.repository.EligibilityTreeRepository;
import kg.sot.reception.repository.SiteContentRepository;
import kg.sot.reception.repository.SurveyConfigRepository;
import kg.sot.reception.util.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CmsService {

    private final CalendarSettingsRepository calendarSettingsRepository;
    private final SiteContentRepository siteContentRepository;
    private final EligibilityTreeRepository eligibilityTreeRepository;
    private final SurveyConfigRepository surveyConfigRepository;

    public CmsService(
            CalendarSettingsRepository calendarSettingsRepository,
            SiteContentRepository siteContentRepository,
            EligibilityTreeRepository eligibilityTreeRepository,
            SurveyConfigRepository surveyConfigRepository
    ) {
        this.calendarSettingsRepository = calendarSettingsRepository;
        this.siteContentRepository = siteContentRepository;
        this.eligibilityTreeRepository = eligibilityTreeRepository;
        this.surveyConfigRepository = surveyConfigRepository;
    }

    @Transactional(readOnly = true)
    public CalendarSettings getCalendarEntity() {
        return calendarSettingsRepository.findById(CalendarSettings.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Настройки календаря не инициализированы"));
    }

    @Transactional(readOnly = true)
    public CalendarSettingsDto getCalendar() {
        return Mappers.toCalendarSettingsDto(getCalendarEntity());
    }

    @Transactional
    public CalendarSettingsDto putCalendar(CalendarSettingsDto dto) {
        CalendarSettings entity = getCalendarEntity();
        Mappers.applyCalendarDto(entity, dto);
        entity.setUpdatedAt(OffsetDateTime.now());
        return Mappers.toCalendarSettingsDto(calendarSettingsRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSiteContent() {
        return new LinkedHashMap<>(siteContentRepository.findById(SiteContent.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Тексты сайта не инициализированы"))
                .getContent());
    }

    @Transactional
    public Map<String, Object> putSiteContent(Map<String, Object> content) {
        SiteContent entity = siteContentRepository.findById(SiteContent.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Тексты сайта не инициализированы"));
        entity.setContent(new LinkedHashMap<>(content));
        entity.setUpdatedAt(OffsetDateTime.now());
        return new LinkedHashMap<>(siteContentRepository.save(entity).getContent());
    }

    @Transactional(readOnly = true)
    public List<EligibilityNode> getEligibilityTree() {
        return new ArrayList<>(eligibilityTreeRepository.findById(EligibilityTree.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Дерево допуска не инициализировано"))
                .getNodes());
    }

    @Transactional
    public List<EligibilityNode> putEligibilityTree(List<EligibilityNode> nodes) {
        EligibilityTree entity = eligibilityTreeRepository.findById(EligibilityTree.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Дерево допуска не инициализировано"));
        entity.setNodes(new ArrayList<>(nodes));
        entity.setUpdatedAt(OffsetDateTime.now());
        return new ArrayList<>(eligibilityTreeRepository.save(entity).getNodes());
    }

    @Transactional(readOnly = true)
    public SurveyBundle getSurvey() {
        SurveyConfig entity = surveyConfigRepository.findById(SurveyConfig.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Опросник не инициализирован"));
        return Mappers.toSurveyBundle(entity);
    }

    @Transactional
    public SurveyBundle putSurvey(SurveyBundle bundle) {
        SurveyConfig entity = surveyConfigRepository.findById(SurveyConfig.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Опросник не инициализирован"));
        entity.setMeta(bundle.meta());
        entity.setQuestions(new ArrayList<>(bundle.questions()));
        entity.setUpdatedAt(OffsetDateTime.now());
        return Mappers.toSurveyBundle(surveyConfigRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PublicBootstrap bootstrap() {
        return new PublicBootstrap(getSiteContent(), getEligibilityTree(), getCalendar(), getSurvey());
    }
}
