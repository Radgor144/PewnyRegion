package com.pewnyregion.region.data.service.controller;

import com.pewnyregion.region.data.service.model.CountyDetailsRequest;
import com.pewnyregion.region.data.service.model.CountyDetailsResponse;
import com.pewnyregion.region.data.service.service.CountyDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/countyDetails")
public class CountyDetailsController {

    private final CountyDetailsService countyDetailsService;

    @GetMapping
    public Mono<CountyDetailsResponse> getCountyDetails(@Valid @ModelAttribute CountyDetailsRequest request) {
        return countyDetailsService.getCountyDetails(request.terytCode(), request.bdlVariableIds());
    }
}
