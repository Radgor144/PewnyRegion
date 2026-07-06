package com.pewnyregion.region.analytics.service.controller;

import com.pewnyregion.region.analytics.service.model.JobResponse;
import com.pewnyregion.region.analytics.service.model.TargetedImportRequest;
import com.pewnyregion.region.analytics.service.service.ImportJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/imports")
public class ImportJobController {

    private final ImportJobService importJobService;

    @PostMapping("/teryt")
    public Mono<ResponseEntity<JobResponse>> importCounties() {
        return importJobService.submitCountiesImport()
                               .map(ResponseEntity.accepted()::body);
    }

    @PostMapping("/full")
    public Mono<ResponseEntity<JobResponse>> createFullImport() {
        return importJobService.submitFullImport()
                               .map(ResponseEntity.accepted()::body);
    }

    @PostMapping("/targeted")
    public Mono<ResponseEntity<JobResponse>> createTargetedImport(@RequestBody TargetedImportRequest request) {
        return importJobService.submitTargetedImport(request)
                               .map(ResponseEntity.accepted()::body);
    }

    @GetMapping("/{jobId}")
    public Mono<ResponseEntity<JobResponse>> getStatus(@PathVariable String jobId) {
        return importJobService.getJobResponse(jobId)
                               .map(ResponseEntity::ok)
                               .onErrorReturn(IllegalArgumentException.class, ResponseEntity.notFound().build());
    }
}