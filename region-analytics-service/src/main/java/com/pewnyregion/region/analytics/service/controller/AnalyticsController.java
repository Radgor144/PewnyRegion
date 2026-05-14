package com.pewnyregion.region.analytics.service.controller;

import com.pewnyregion.region.analytics.service.model.PopulationResponse;
import com.pewnyregion.region.analytics.service.service.DemographicsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final DemographicsService demographicsService;

    @GetMapping("/population/{teryt}")
    public Mono<PopulationResponse> getPopulation(@PathVariable String teryt) {
        return demographicsService.getPopulationByTeryt(teryt);
    }
}