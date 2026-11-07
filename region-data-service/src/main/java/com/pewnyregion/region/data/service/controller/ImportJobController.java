package com.pewnyregion.region.data.service.controller;

import com.pewnyregion.region.data.service.model.JobResponse;
import com.pewnyregion.region.data.service.model.TargetedImportRequest;
import com.pewnyregion.region.data.service.service.BdlStatisticalDataImportService;
import com.pewnyregion.region.data.service.service.BdlUnitMappingImportService;
import com.pewnyregion.region.data.service.service.ImportJobService;
import com.pewnyregion.region.data.service.service.TercCatalogImportService;
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
    private final TercCatalogImportService tercCatalogImportService;
    private final BdlUnitMappingImportService bdlUnitMappingImportService;
    private final BdlStatisticalDataImportService bdlStatisticalDataImportService;

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
    public Mono<ResponseEntity<JobResponse>> createTargetedImport(@Valid @RequestBody TargetedImportRequest request) {
        return importJobService.submitTargetedImport(request)
                               .map(ResponseEntity.accepted()::body);
    }

    @GetMapping("/{jobId}")
    public Mono<ResponseEntity<JobResponse>> getStatus(@PathVariable String jobId) {
        return importJobService.getJobResponse(jobId)
                               .map(ResponseEntity::ok)
                               .onErrorReturn(IllegalArgumentException.class, ResponseEntity.notFound().build());
    }

    @GetMapping("/xml")
    public Mono<Void> getXml() {
        return tercCatalogImportService.importCatalog();
    }

    @GetMapping("/test")
    public Mono<Void> gettest() {
        return bdlUnitMappingImportService.importMappings();
    }

    @PostMapping("/test/bdl/{terytCode}")
    public Mono<ResponseEntity<Integer>> testBdlImport(
            @PathVariable String terytCode
    ) {
        return bdlStatisticalDataImportService.importUnit(terytCode)
                                              .map(ResponseEntity::ok);
    }
}