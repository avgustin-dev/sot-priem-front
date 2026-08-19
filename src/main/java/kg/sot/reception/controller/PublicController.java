package kg.sot.reception.controller;

import jakarta.validation.Valid;
import kg.sot.reception.dto.AvailableDatesResponse;
import kg.sot.reception.dto.BookAppointmentRequest;
import kg.sot.reception.dto.BookAppointmentResponse;
import kg.sot.reception.dto.CitizenActionRequest;
import kg.sot.reception.dto.FeedbackRequest;
import kg.sot.reception.dto.PublicAppointment;
import kg.sot.reception.dto.PublicAppointmentLookup;
import kg.sot.reception.dto.PublicBootstrap;
import kg.sot.reception.dto.RecoverCodesRequest;
import kg.sot.reception.dto.RecoverCodesResponse;
import kg.sot.reception.dto.SlotDayResponse;
import kg.sot.reception.dto.SubmitSurveyRequest;
import kg.sot.reception.dto.SurveyBundle;
import kg.sot.reception.dto.UnlockRequest;
import kg.sot.reception.service.AppealService;
import kg.sot.reception.service.AppointmentService;
import kg.sot.reception.service.CmsService;
import kg.sot.reception.service.SlotService;
import kg.sot.reception.service.SurveyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    private final CmsService cmsService;
    private final SlotService slotService;
    private final AppointmentService appointmentService;
    private final AppealService appealService;
    private final SurveyService surveyService;

    public PublicController(
            CmsService cmsService,
            SlotService slotService,
            AppointmentService appointmentService,
            AppealService appealService,
            SurveyService surveyService
    ) {
        this.cmsService = cmsService;
        this.slotService = slotService;
        this.appointmentService = appointmentService;
        this.appealService = appealService;
        this.surveyService = surveyService;
    }

    @GetMapping("/bootstrap")
    public PublicBootstrap bootstrap() {
        return cmsService.bootstrap();
    }

    @GetMapping("/dates")
    public AvailableDatesResponse dates(@RequestParam String targetId) {
        return slotService.availableDates(targetId);
    }

    @GetMapping("/slots")
    public SlotDayResponse slots(
            @RequestParam LocalDate date,
            @RequestParam String targetId,
            @RequestParam(required = false) String excludeAppointmentId
    ) {
        return slotService.slotsForDate(date, targetId, excludeAppointmentId);
    }

    @PostMapping("/appointments")
    public ResponseEntity<BookAppointmentResponse> book(@Valid @RequestBody BookAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(request));
    }

    @GetMapping("/appointments/{code}")
    public PublicAppointmentLookup lookup(@PathVariable String code) {
        return appointmentService.lookup(code);
    }

    @PostMapping("/appointments/{code}/unlock")
    public PublicAppointment unlock(@PathVariable String code, @Valid @RequestBody UnlockRequest request) {
        return appointmentService.unlock(code, request.pin());
    }

    @PostMapping("/appointments/{code}/actions")
    public PublicAppointment actions(@PathVariable String code, @Valid @RequestBody CitizenActionRequest request) {
        return appointmentService.citizenAction(code, request);
    }

    @PostMapping("/appointments/{code}/feedback")
    public ResponseEntity<Void> feedback(@PathVariable String code, @Valid @RequestBody FeedbackRequest request) {
        appealService.submitFeedback(code, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recover-codes")
    public RecoverCodesResponse recoverCodes(@Valid @RequestBody RecoverCodesRequest request) {
        return appointmentService.recoverCodes(request.phone());
    }

    @GetMapping("/survey")
    public SurveyBundle survey() {
        return surveyService.getSurvey();
    }

    @PostMapping("/survey")
    public ResponseEntity<Void> submitSurvey(@Valid @RequestBody SubmitSurveyRequest request) {
        surveyService.submitResponse(request);
        return ResponseEntity.noContent().build();
    }
}
