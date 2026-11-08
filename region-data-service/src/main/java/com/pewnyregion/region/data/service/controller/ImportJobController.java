package com.pewnyregion.region.data.service.controller;

import com.pewnyregion.region.data.service.model.JobResponse;
import com.pewnyregion.region.data.service.model.TargetedImportRequest;
import com.pewnyregion.region.data.service.service.ImportJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/imports")
public class ImportJobController {

    private final ImportJobService importJobService;

    @PostMapping("/teryt/catalog")
    public Mono<ResponseEntity<JobResponse>> importTerytCatalog() {
        return importJobService.submitTerytCatalogImport()
                               .map(ResponseEntity.accepted()::body);
    }

    @PostMapping("/bdl/unit-mappings")
    public Mono<ResponseEntity<JobResponse>> importBdlUnitMappings() {
        return importJobService.submitBdlUnitMappingsImport()
                               .map(ResponseEntity.accepted()::body);
    }

    @PostMapping("/bdl/counties")
    public Mono<ResponseEntity<JobResponse>> importAllCounties() {
        return importJobService.submitFullImport()
                               .map(ResponseEntity.accepted()::body);
    }

    @PostMapping("/bdl/counties/{terytCode}")
    public Mono<ResponseEntity<JobResponse>> importCounty(
            @PathVariable String terytCode
    ) {
        return importJobService.submitBdlUnitImport(terytCode)
                               .map(ResponseEntity.accepted()::body);
    }

    @PostMapping("/bdl/targeted")
    public Mono<ResponseEntity<JobResponse>> importTargeted(
            @Valid @RequestBody TargetedImportRequest request
    ) {
        return importJobService.submitTargetedImport(request)
                               .map(ResponseEntity.accepted()::body);
    }

    @PostMapping("/normalization")
    public Mono<ResponseEntity<JobResponse>> normalize() {
        return importJobService.submitNormalization()
                               .map(ResponseEntity.accepted()::body);
    }

    @GetMapping("/{jobId}")
    public Mono<ResponseEntity<JobResponse>> getStatus(
            @PathVariable String jobId
    ) {
        return importJobService.getJobResponse(jobId)
                               .map(ResponseEntity::ok)
                               .onErrorReturn(
                                       IllegalArgumentException.class,
                                       ResponseEntity.notFound().build()
                               );
    }
}