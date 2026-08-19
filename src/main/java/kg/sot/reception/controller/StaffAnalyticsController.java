package kg.sot.reception.controller;

import kg.sot.reception.dto.AnalyticsResponse;
import kg.sot.reception.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff/analytics")
public class StaffAnalyticsController {

    private final AnalyticsService analyticsService;

    public StaffAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public AnalyticsResponse analytics() {
        return analyticsService.analytics();
    }
}
