package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.component.BackgroundJobQueue;
import com.pewnyregion.region.analytics.service.entity.ImportJobEntity;
import com.pewnyregion.region.analytics.service.model.JobResponse;
import com.pewnyregion.region.analytics.service.model.TargetedImportRequest;
import com.pewnyregion.region.analytics.service.model.consts.ImportJobStatus;
import com.pewnyregion.region.analytics.service.model.consts.ImportJobType;
import com.pewnyregion.region.analytics.service.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
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
        return ensureNotRunning(ImportJobType.FULL)
                .then(createAndEnqueue(
                        ImportJobType.FULL,
                        dataImportService.runImport(null, null)
                                         .flatMap(init -> normalizationService.calculateAndSaveScoresForAllYears()
                                                                              .map(norm -> init.toMessage() + " | " + norm.toMessage()))
                ));
    }

    public Mono<JobResponse> submitTargetedImport(TargetedImportRequest request) {
        return ensureNotRunning(ImportJobType.TARGETED)
                .then(createAndEnqueue(
                        ImportJobType.TARGETED,
                        dataImportService.runImport(request.apiNames(), request.years())
                                         .flatMap(init -> normalizationService.calculateAndSaveScoresForYears(request.years())
                                                                              .map(norm -> init.toMessage() + " | " + norm.toMessage()))
                ));
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

    private Mono<Void> ensureNotRunning(ImportJobType type) {
        return repository.countByJobTypeAndStatusIn(type, List.of(ImportJobStatus.RUNNING, ImportJobStatus.PENDING))
                         .flatMap(running -> running > 0
                                 ? Mono.error(new IllegalStateException("Job already in progress or enqueued: " + type))
                                 : Mono.empty());
    }

    private Mono<JobResponse> createAndEnqueue(ImportJobType type, Mono<String> task) {
        return repository.save(buildPendingJob(type))
                         .doOnNext(job -> jobQueue.enqueueJob(job.getId(), task))
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