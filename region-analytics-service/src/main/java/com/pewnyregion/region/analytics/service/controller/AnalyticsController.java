package com.pewnyregion.region.analytics.service.controller;

import com.pewnyregion.region.analytics.service.model.AnalyticsResponse;
import com.pewnyregion.region.analytics.service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.pewnyregion.region.analytics.service.model.BdlVarId.fromApiNames;
import static com.pewnyregion.region.analytics.service.model.BdlVarId.getAllBdlVarIds;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{terytCode}")
    public Mono<List<AnalyticsResponse>> getAnalytics(@PathVariable String terytCode,
                                                      @RequestParam List<String> variables) {
        return analyticsService.getAnalyticsData(terytCode, fromApiNames(variables));
    }

    @GetMapping("/variables")
    public List<String> getVariables() {
        return getAllBdlVarIds();
    }
}