package kg.sot.reception.controller;

import jakarta.validation.Valid;
import kg.sot.reception.dto.CancelRequest;
import kg.sot.reception.dto.ConfirmRequest;
import kg.sot.reception.dto.PatchAppointmentRequest;
import kg.sot.reception.dto.PublicAppointment;
import kg.sot.reception.dto.RejectRequest;
import kg.sot.reception.dto.RescheduleRequest;
import kg.sot.reception.dto.SetStatusRequest;
import kg.sot.reception.dto.StaffProfile;
import kg.sot.reception.model.AppointmentStatus;
import kg.sot.reception.security.StaffPrincipal;
import kg.sot.reception.service.AppointmentService;
import kg.sot.reception.service.StaffUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffAppointmentController {

    private final AppointmentService appointmentService;
    private final StaffUserService staffUserService;

    public StaffAppointmentController(AppointmentService appointmentService, StaffUserService staffUserService) {
        this.appointmentService = appointmentService;
        this.staffUserService = staffUserService;
    }

    @GetMapping("/appointments")
    public List<PublicAppointment> list(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String targetId
    ) {
        return appointmentService.listStaff(status, date, targetId);
    }

    @PostMapping("/appointments/{id}/confirm")
    public PublicAppointment confirm(
            @PathVariable String id,
            @RequestBody(required = false) ConfirmRequest request,
            @AuthenticationPrincipal StaffPrincipal principal
    ) {
        return appointmentService.confirm(id, request, principal.getUser());
    }

    @PostMapping("/appointments/{id}/reject")
    public PublicAppointment reject(
            @PathVariable String id,
            @Valid @RequestBody RejectRequest request,
            @AuthenticationPrincipal StaffPrincipal principal
    ) {
        return appointmentService.reject(id, request, principal.getUser());
    }

    @PostMapping("/appointments/{id}/cancel")
    public PublicAppointment cancel(
            @PathVariable String id,
            @RequestBody(required = false) CancelRequest request,
            @AuthenticationPrincipal StaffPrincipal principal
    ) {
        return appointmentService.staffCancel(id, request, principal.getUser());
    }

    @PostMapping("/appointments/{id}/restore")
    public PublicAppointment restore(@PathVariable String id, @AuthenticationPrincipal StaffPrincipal principal) {
        return appointmentService.staffRestore(id, principal.getUser());
    }

    @PatchMapping("/appointments/{id}")
    public PublicAppointment patch(
            @PathVariable String id,
            @Valid @RequestBody PatchAppointmentRequest request,
            @AuthenticationPrincipal StaffPrincipal principal
    ) {
        return appointmentService.patch(id, request, principal.getUser());
    }

    @PostMapping("/appointments/{id}/status")
    public PublicAppointment setStatus(
            @PathVariable String id,
            @Valid @RequestBody SetStatusRequest request,
            @AuthenticationPrincipal StaffPrincipal principal
    ) {
        return appointmentService.setStatus(id, request, principal.getUser());
    }

    @PostMapping("/appointments/{id}/reschedule")
    public PublicAppointment reschedule(
            @PathVariable String id,
            @Valid @RequestBody RescheduleRequest request,
            @AuthenticationPrincipal StaffPrincipal principal
    ) {
        return appointmentService.staffReschedule(id, request, principal.getUser());
    }

    @GetMapping("/users")
    public List<StaffProfile> users() {
        return staffUserService.list();
    }
}
