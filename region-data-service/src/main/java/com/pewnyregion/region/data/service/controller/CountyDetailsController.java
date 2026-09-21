package com.pewnyregion.region.data.service.controller;

import com.pewnyregion.region.data.service.model.CountyDetailsResponse;
import com.pewnyregion.region.data.service.service.CountyDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/countyDetails")
public class CountyDetailsController {

    private final CountyDetailsService countyDetailsService;

    @GetMapping
    public Mono<CountyDetailsResponse> getCountyDetails(@RequestParam String terytCode, @RequestParam List<Integer> bdlVariableIds) {
        return countyDetailsService.getCountyDetails(terytCode, bdlVariableIds);
    }
}
