package com.pewnyregion.region.analytics.service.controller;

import com.pewnyregion.region.analytics.service.model.MapRequest;
import com.pewnyregion.region.analytics.service.model.MapResponse;
import com.pewnyregion.region.analytics.service.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class MapController {

    private final MapService mapService;

    @PostMapping
    public Flux<MapResponse> getMap(@RequestBody MapRequest request) {
        return mapService.getMapData(
                request.apiNames(),
                request.yearFrom(),
                request.yearTo()
        );
    }
}