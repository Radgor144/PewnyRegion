package com.pewnyregion.region.analytics.service.controller;

import com.pewnyregion.region.analytics.service.model.MapRequest;
import com.pewnyregion.region.analytics.service.model.MapResponse;
import com.pewnyregion.region.analytics.service.service.MapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class MapController {

    private final MapService mapService;

    @PostMapping("/county-scores")
    public Flux<MapResponse> getCountyScores(@Valid @RequestBody MapRequest request) {
        return mapService.getMapData(request);
    }
}