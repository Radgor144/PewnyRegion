package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.component.JobExecutor;
import com.pewnyregion.region.analytics.service.entity.ImportJobEntity;
import com.pewnyregion.region.analytics.service.model.*;
import com.pewnyregion.region.analytics.service.model.consts.ImportJobStatus;
import com.pewnyregion.region.analytics.service.model.consts.ImportJobType;
import com.pewnyregion.region.analytics.service.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportJobService {

    private final ImportJobRepository repository;
    private final BdlFullInitializationService fullService;
    private final BdlTargetedUpdateService targetedService;
    private final CountyImportService countyImportService;
    private final NormalizationService normalizationService;
    private final JobExecutor jobExecutor;

    public Mono<JobResponse> submitFullImport() {
        return repository.countByJobTypeAndStatus(ImportJobType.FULL, ImportJobStatus.RUNNING)
                         .flatMap(running -> {
                             if (running > 0) {
                                 return Mono.error(new IllegalStateException("Full import is already running"));
                             }
                             return createAndSubmit(ImportJobType.FULL,
                                     fullService.runFullInitialization()
                                                .flatMap(init -> normalizationService.calculateAndSaveScoresForAllYears()
                                                                                     .map(norm -> init.toMessage() + " | " + norm.toMessage()))
                             );
                         });
    }

    public Mono<JobResponse> submitTargetedImport(TargetedImportRequest request) {
        return repository.countByJobTypeAndStatus(ImportJobType.TARGETED, ImportJobStatus.RUNNING)
                         .flatMap(running -> {
                             if (running > 0) {
                                 return Mono.error(new IllegalStateException("Targeted import is already running"));
                             }
                             return createAndSubmit(ImportJobType.TARGETED,
                                     targetedService.runTargetedUpdate(request.apiNames(), request.years())
                                                    .flatMap(init -> normalizationService.calculateAndSaveScoresForYears(request.years())
                                                                                         .map(norm -> init.toMessage() + " | " + norm.toMessage()))
                             );
                         });
    }

    public Mono<JobResponse> submitCountiesImport() {
        return createAndSubmit(ImportJobType.COUNTIES,
                countyImportService.runImportLogic().thenReturn("Counties imported successfully")
        );
    }

    public Mono<JobResponse> getJobResponse(String id) {
        return repository.findById(id)
                         .map(this::toResponse)
                         .switchIfEmpty(Mono.error(new IllegalArgumentException("Job not found: " + id)));
    }

    private Mono<JobResponse> createAndSubmit(ImportJobType type, Mono<String> task) {
        return repository.save(buildPendingJob(type))
                         .map(job -> {
                             jobExecutor.run(job.getId(), task);
                             return toResponse(job);
                         });
    }

    private ImportJobEntity buildPendingJob(ImportJobType type) {
        return ImportJobEntity.builder()
                              .id(UUID.randomUUID().toString())
                              .jobType(type)
                              .status(ImportJobStatus.PENDING)
                              .startedAt(LocalDateTime.now())
                              .message("Import submitted")
                              .isNew(true)
                              .build();
    }

    private JobResponse toResponse(ImportJobEntity job) {
        return new JobResponse(job.getId(), job.getStatus().name(), job.getJobType().name());
    }
}