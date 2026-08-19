package kg.sot.reception.controller;

import jakarta.validation.Valid;
import kg.sot.reception.dto.SurveyBundle;
import kg.sot.reception.dto.SurveyResponseDto;
import kg.sot.reception.service.SurveyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff/survey")
public class StaffSurveyController {

    private final SurveyService surveyService;

    public StaffSurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping
    public SurveyBundle get() {
        return surveyService.getSurvey();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SurveyBundle put(@Valid @RequestBody SurveyBundle request) {
        return surveyService.putSurvey(request);
    }

    @GetMapping("/responses")
    public List<SurveyResponseDto> responses() {
        return surveyService.listResponses();
    }
}
