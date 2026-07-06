package com.pewnyregion.region.analytics.service.controller;

import com.pewnyregion.region.analytics.service.model.VariableResponse;
import com.pewnyregion.region.analytics.service.service.VariableService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/variables")
public class VariableController {

    private final VariableService variableService;

    @GetMapping
    public Flux<VariableResponse> getAllVariables() {
        return variableService.getAllVariables();
    }
}
