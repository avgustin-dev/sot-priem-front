package kg.sot.reception.controller;

import jakarta.validation.Valid;
import kg.sot.reception.dto.AddControlLogRequest;
import kg.sot.reception.dto.AppealCardDto;
import kg.sot.reception.dto.CompletePrepRequest;
import kg.sot.reception.dto.CompleteReceptionRequest;
import kg.sot.reception.dto.SetAppealStageRequest;
import kg.sot.reception.dto.SetAssignmentStatusRequest;
import kg.sot.reception.dto.SubmitFinalAnswerRequest;
import kg.sot.reception.model.AppealStage;
import kg.sot.reception.security.StaffPrincipal;
import kg.sot.reception.service.AppealService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff/appeals")
public class StaffAppealController {

    private final AppealService appealService;

    public StaffAppealController(AppealService appealService) {
        this.appealService = appealService;
    }

    @GetMapping
    public List<AppealCardDto> list(@RequestParam(required = false) AppealStage stage) {
        return appealService.list(stage);
    }

    @GetMapping("/{id}")
    public AppealCardDto get(@PathVariable String id) {
        return appealService.get(id);
    }

    @PostMapping("/{id}/prep")
    public AppealCardDto completePrep(
            @PathVariable String id,
            @Valid @RequestBody CompletePrepRequest request,
            @AuthenticationPrincipal StaffPrincipal principal
    ) {
        return appealService.completePrep(id, request, principal.getUser());
    }

    @PostMapping("/{id}/ready")
    public AppealCardDto markReady(@PathVariable String id) {
        return appealService.markReady(id);
    }

    @PostMapping("/{id}/reception")
    public AppealCardDto completeReception(
            @PathVariable String id,
            @Valid @RequestBody CompleteReceptionRequest request,
            @AuthenticationPrincipal StaffPrincipal principal
    ) {
        return appealService.completeReception(id, request, principal.getUser());
    }

    @PostMapping("/{id}/control")
    public AppealCardDto addControlLog(
            @PathVariable String id,
            @Valid @RequestBody AddControlLogRequest request,
            @AuthenticationPrincipal StaffPrincipal principal
    ) {
        return appealService.addControlLog(id, request, principal.getUser());
    }

    @PostMapping("/{id}/assignment-status")
    public AppealCardDto setAssignmentStatus(
            @PathVariable String id,
            @Valid @RequestBody SetAssignmentStatusRequest request
    ) {
        return appealService.setAssignmentStatus(id, request);
    }

    @PostMapping("/{id}/answer")
    public AppealCardDto submitFinalAnswer(
            @PathVariable String id,
            @Valid @RequestBody SubmitFinalAnswerRequest request,
            @AuthenticationPrincipal StaffPrincipal principal
    ) {
        return appealService.submitFinalAnswer(id, request, principal.getUser());
    }

    @PostMapping("/{id}/stage")
    public AppealCardDto setStage(
            @PathVariable String id,
            @Valid @RequestBody SetAppealStageRequest request,
            @AuthenticationPrincipal StaffPrincipal principal
    ) {
        return appealService.setStage(id, request, principal.getUser());
    }
}
