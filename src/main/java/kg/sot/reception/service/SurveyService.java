package kg.sot.reception.service;

import kg.sot.reception.dto.SubmitSurveyRequest;
import kg.sot.reception.dto.SurveyBundle;
import kg.sot.reception.dto.SurveyResponseDto;
import kg.sot.reception.exception.ApiException;
import kg.sot.reception.model.SurveyResponseEntity;
import kg.sot.reception.repository.SurveyResponseRepository;
import kg.sot.reception.util.IdGenerator;
import kg.sot.reception.util.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class SurveyService {

    private final CmsService cmsService;
    private final SurveyResponseRepository surveyResponseRepository;

    public SurveyService(CmsService cmsService, SurveyResponseRepository surveyResponseRepository) {
        this.cmsService = cmsService;
        this.surveyResponseRepository = surveyResponseRepository;
    }

    @Transactional(readOnly = true)
    public SurveyBundle getSurvey() {
        return cmsService.getSurvey();
    }

    @Transactional
    public SurveyBundle putSurvey(SurveyBundle bundle) {
        return cmsService.putSurvey(bundle);
    }

    @Transactional
    public void submitResponse(SubmitSurveyRequest req) {
        if (req.answers() == null || req.answers().isEmpty()) {
            throw ApiException.validation("Нет ответов");
        }
        String courtName = req.courtName() != null && !req.courtName().isBlank()
                ? req.courtName()
                : cmsService.getSurvey().meta().courtNameRu();
        SurveyResponseEntity entity = SurveyResponseEntity.builder()
                .id(IdGenerator.next("sr"))
                .submittedAt(OffsetDateTime.now(SlotService.BISHKEK))
                .courtName(courtName)
                .answers(new LinkedHashMap<>(req.answers()))
                .build();
        surveyResponseRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<SurveyResponseDto> listResponses() {
        return surveyResponseRepository.findAllByOrderBySubmittedAtDesc().stream()
                .map(Mappers::toSurveyResponseDto)
                .toList();
    }
}
