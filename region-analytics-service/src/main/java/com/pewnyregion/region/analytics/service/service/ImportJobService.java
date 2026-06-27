package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.entity.ImportJobEntity;
import com.pewnyregion.region.analytics.service.model.ImportJobStatus;
import com.pewnyregion.region.analytics.service.model.ImportJobType;
import com.pewnyregion.region.analytics.service.model.JobResponse;
import com.pewnyregion.region.analytics.service.model.TargetedImportRequest;
import com.pewnyregion.region.analytics.service.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportJobService {

    private final ImportJobRepository repository;
    private final BdlFullInitializationService fullService;
    private final BdlTargetedUpdateService targetedService;
    private final CountyImportService countyImportService;

    public Mono<JobResponse> submitCountiesImport() {
        return submitTask(ImportJobType.COUNTIES, countyImportService.runImportLogic());
    }

    public Mono<JobResponse> submitFullImport() {
        return submitTask(ImportJobType.FULL, fullService.runFullInitialization().then());
    }

    public Mono<JobResponse> submitTargetedImport(TargetedImportRequest request) {
        return submitTask(ImportJobType.TARGETED, targetedService.runTargetedUpdate(request.apiNames(), request.years()).then());
    }

    public Mono<JobResponse> getJobResponse(String id) {
        return repository.findById(id)
                         .map(this::toResponse);
    }

    private Mono<JobResponse> submitTask(ImportJobType type, Mono<Void> task) {
        ImportJobEntity job = buildPendingJob(type);

        return repository.save(job)
                         .doOnError(e -> log.error("Database save error: ", e))
                         .doOnNext(savedJob -> executeAsync(savedJob.getId(), task))
                         .map(this::toResponse);
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

    private void executeAsync(String jobId, Mono<Void> task) {
        markRunning(jobId).then(task)
                          .then(markCompleted(jobId))
                          .onErrorResume(error -> {
                              log.error("Job {} failed: {}", jobId, error.getMessage());
                              return markFailed(jobId, error.getMessage());
                          })
                          .subscribeOn(Schedulers.boundedElastic())
                          .subscribe();
    }

    private Mono<Void> updateJobStatus(String jobId, Consumer<ImportJobEntity> jobModifier) {
        return repository.findById(jobId)
                         .doOnNext(jobModifier)
                         .flatMap(repository::save)
                         .then();
    }

    private Mono<Void> markRunning(String jobId) {
        return updateJobStatus(jobId, job -> {
            job.setStatus(ImportJobStatus.RUNNING);
            job.setStartedAt(LocalDateTime.now());
            job.setMessage("Import in progress");
        });
    }

    private Mono<Void> markCompleted(String jobId) {
        return updateJobStatus(jobId, job -> {
            job.setStatus(ImportJobStatus.COMPLETED);
            job.setFinishedAt(LocalDateTime.now());
            job.setMessage("Import completed successfully");
        });
    }

    private Mono<Void> markFailed(String jobId, String error) {
        return updateJobStatus(jobId, job -> {
            job.setStatus(ImportJobStatus.FAILED);
            job.setFinishedAt(LocalDateTime.now());
            job.setMessage(error);
        });
    }

    private JobResponse toResponse(ImportJobEntity job) {
        return new JobResponse(job.getId(), job.getStatus().name(), job.getJobType().name());
    }
}