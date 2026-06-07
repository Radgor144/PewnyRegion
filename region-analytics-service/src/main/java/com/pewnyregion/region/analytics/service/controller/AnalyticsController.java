package com.pewnyregion.region.analytics.service.controller;

import com.pewnyregion.region.analytics.service.model.AnalyticsResponse;
import com.pewnyregion.region.analytics.service.model.BdlVariableDto;
import com.pewnyregion.region.analytics.service.service.AnalyticsService;
import com.pewnyregion.region.analytics.service.service.BdlVariableService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final BdlVariableService bdlVariableService;

    @GetMapping("/{terytCode}")
    public Mono<List<AnalyticsResponse>> getAnalytics(@PathVariable String terytCode,
                                                      @RequestParam List<String> variables) {
        return analyticsService.getAnalyticsData(terytCode, variables);
    }

    @GetMapping("/variables")
    public Flux<BdlVariableDto> getVariables() {
        return bdlVariableService.getAllVariablesWithIds();
    }
}