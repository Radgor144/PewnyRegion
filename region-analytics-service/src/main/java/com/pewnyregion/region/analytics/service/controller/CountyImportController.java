package com.pewnyregion.region.analytics.service.controller;

import com.pewnyregion.region.analytics.service.service.CountyImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/counties")
public class CountyImportController {

    private final CountyImportService countyImportService;

    @GetMapping("/import")
    public Mono<Void> importCounties() {
        return countyImportService.importCounties();
    }
}
