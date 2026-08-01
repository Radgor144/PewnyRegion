package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.component.BackgroundJobQueue;
import com.pewnyregion.region.analytics.service.entity.ImportJobEntity;
import com.pewnyregion.region.analytics.service.exception.ConflictException;
import com.pewnyregion.region.analytics.service.model.JobResponse;
import com.pewnyregion.region.analytics.service.model.TargetedImportRequest;
import com.pewnyregion.region.analytics.service.model.consts.ImportJobStatus;
import com.pewnyregion.region.analytics.service.model.consts.ImportJobType;
import com.pewnyregion.region.analytics.service.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportJobService {

    private final ImportJobRepository repository;
    private final BdlDataImportService dataImportService;
    private final CountyImportService countyImportService;
    private final NormalizationService normalizationService;
    private final BackgroundJobQueue jobQueue;

    public Mono<JobResponse> submitFullImport() {
        return createAndEnqueue(ImportJobType.FULL,
                dataImportService.runFullImport()
                                 .flatMap(importSummary -> normalizationService.calculateAndSaveScoresForAllYears()
                                                                      .map(normSummary -> importSummary.toMessage() + " | " + normSummary.toMessage())));
    }

    public Mono<JobResponse> submitTargetedImport(TargetedImportRequest request) {
        return createAndEnqueue(ImportJobType.TARGETED,
                dataImportService.runTargetedImport(request.apiNames(), request.years())
                                 .flatMap(importSummary -> normalizationService.calculateAndSaveScoresForYears(request.years())
                                                                      .map(normSummary -> importSummary.toMessage() + " | " + normSummary.toMessage())));
    }

    public Mono<JobResponse> submitCountiesImport() {
        return createAndEnqueue(ImportJobType.COUNTIES,
                countyImportService.runImportLogic().thenReturn("Counties imported successfully")
        );
    }

    public Mono<JobResponse> getJobResponse(String id) {
        return repository.findById(id)
                         .map(this::toResponse)
                         .switchIfEmpty(Mono.error(new IllegalArgumentException("Job not found: " + id)));
    }

    private Mono<JobResponse> createAndEnqueue(ImportJobType type, Mono<String> task) {
        return repository.save(buildPendingJob(type))
                         .onErrorMap(DataIntegrityViolationException.class,
                                 e -> new ConflictException("Another import is already running or pending"))
                         .flatMap(job -> jobQueue.enqueueJob(job.getId(), task).thenReturn(job))
                         .map(this::toResponse);
    }

    private ImportJobEntity buildPendingJob(ImportJobType type) {
        return ImportJobEntity.builder()
                              .id(UUID.randomUUID().toString())
                              .jobType(type)
                              .status(ImportJobStatus.PENDING)
                              .startedAt(LocalDateTime.now())
                              .message("Import enqueued")
                              .isNew(true)
                              .build();
    }

    private JobResponse toResponse(ImportJobEntity job) {
        return new JobResponse(job.getId(), job.getStatus().name(), job.getJobType().name());
    }
}