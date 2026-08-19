package kg.sot.reception.controller;

import jakarta.validation.Valid;
import kg.sot.reception.dto.CalendarSettingsDto;
import kg.sot.reception.dto.EligibilityPayload;
import kg.sot.reception.service.CmsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffCmsController {

    private final CmsService cmsService;

    public StaffCmsController(CmsService cmsService) {
        this.cmsService = cmsService;
    }

    @GetMapping("/calendar")
    public CalendarSettingsDto getCalendar() {
        return cmsService.getCalendar();
    }

    @PutMapping("/calendar")
    @PreAuthorize("hasRole('ADMIN')")
    public CalendarSettingsDto putCalendar(@Valid @RequestBody CalendarSettingsDto request) {
        return cmsService.putCalendar(request);
    }

    @GetMapping("/content")
    public Map<String, Object> getContent() {
        return cmsService.getSiteContent();
    }

    @PutMapping("/content")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> putContent(@RequestBody Map<String, Object> request) {
        return cmsService.putSiteContent(request);
    }

    @GetMapping("/eligibility")
    public EligibilityPayload getEligibility() {
        return new EligibilityPayload(cmsService.getEligibilityTree());
    }

    @PutMapping("/eligibility")
    @PreAuthorize("hasRole('ADMIN')")
    public EligibilityPayload putEligibility(@Valid @RequestBody EligibilityPayload request) {
        return new EligibilityPayload(cmsService.putEligibilityTree(request.nodes()));
    }
}
