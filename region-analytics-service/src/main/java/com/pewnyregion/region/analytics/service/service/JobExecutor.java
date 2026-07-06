package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.entity.ImportJobEntity;
import com.pewnyregion.region.analytics.service.model.ImportJobStatus;
import com.pewnyregion.region.analytics.service.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobExecutor {

    private final ImportJobRepository repository;

    public void run(String jobId, Mono<String> task) {
        markRunning(jobId)
                .then(task)
                .flatMap(message -> markCompleted(jobId, message))
                .onErrorResume(error -> {
                    log.error("[JOB] {} failed: {}", jobId, error.getMessage());
                    return markFailed(jobId, error.getMessage());
                })
                .subscribe();
    }

    private Mono<Void> markRunning(String jobId) {
        return updateStatus(jobId, job -> {
            job.setStatus(ImportJobStatus.RUNNING);
            job.setStartedAt(LocalDateTime.now());
            job.setMessage("Import in progress");
        });
    }

    private Mono<Void> markCompleted(String jobId, String message) {
        return updateStatus(jobId, job -> {
            job.setStatus(ImportJobStatus.COMPLETED);
            job.setFinishedAt(LocalDateTime.now());
            job.setMessage(message);
        });
    }

    private Mono<Void> markFailed(String jobId, String error) {
        return updateStatus(jobId, job -> {
            job.setStatus(ImportJobStatus.FAILED);
            job.setFinishedAt(LocalDateTime.now());
            job.setMessage(error);
        });
    }

    private Mono<Void> updateStatus(String jobId, Consumer<ImportJobEntity> modifier) {
        return repository.findById(jobId)
                         .doOnNext(modifier)
                         .flatMap(repository::save)
                         .then();
    }
}