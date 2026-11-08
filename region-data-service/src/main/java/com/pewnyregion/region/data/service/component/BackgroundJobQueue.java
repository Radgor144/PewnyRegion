package com.pewnyregion.region.data.service.component;

import com.pewnyregion.region.data.service.entity.ImportJobEntity;
import com.pewnyregion.region.data.service.model.PendingJob;
import com.pewnyregion.region.data.service.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static com.pewnyregion.region.data.service.model.consts.ImportJobStatus.COMPLETED;
import static com.pewnyregion.region.data.service.model.consts.ImportJobStatus.FAILED;
import static com.pewnyregion.region.data.service.model.consts.ImportJobStatus.RUNNING;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackgroundJobQueue implements DisposableBean {

    private final ImportJobRepository importJobRepository;

    private final Sinks.Many<PendingJob> sink =
            Sinks.many().unicast().onBackpressureBuffer();

    private Disposable subscriptionDisposable;

    public Mono<Void> enqueueJob(String jobId, Mono<String> task) {
        Sinks.EmitResult emitResult = sink.tryEmitNext(
                new PendingJob(jobId, task)
        );

        if (emitResult.isSuccess()) {
            log.info("Job {} enqueued successfully", jobId);
            return Mono.empty();
        }

        log.error(
                "Failed to enqueue job {}: {}",
                jobId,
                emitResult
        );

        return updateJobStatus(jobId, entity -> {
            entity.setStatus(FAILED);
            entity.setFinishedAt(LocalDateTime.now());
            entity.setMessage("Failed to enqueue job: " + emitResult);
        }).then(Mono.error(new IllegalStateException(
                "Failed to enqueue import job: " + emitResult
        )));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startJobProcessor() {
        log.info("Starting job processor");

        subscriptionDisposable = sink.asFlux()
                                     .concatMap(this::processJob)
                                     .subscribe(
                                             ignored -> {
                                             },
                                             error -> log.error(
                                                     "Fatal error in job processor: {}",
                                                     error.getMessage(),
                                                     error
                                             )
                                     );
    }

    @Override
    public void destroy() {
        if (subscriptionDisposable != null
                && !subscriptionDisposable.isDisposed()) {
            log.info("Disposing job processor");
            subscriptionDisposable.dispose();
            sink.tryEmitComplete();
        }
    }

    private Mono<Void> processJob(PendingJob job) {
        return updateJobStatus(job.jobId(), entity -> {
            entity.setStatus(RUNNING);
            entity.setStartedAt(LocalDateTime.now());
            entity.setMessage("Import in progress");
        })
                .then(Mono.defer(job::task))
                .flatMap(message ->
                        updateJobStatus(job.jobId(), entity -> {
                            entity.setStatus(COMPLETED);
                            entity.setFinishedAt(LocalDateTime.now());
                            entity.setMessage(message);
                        })
                )
                .onErrorResume(error -> {
                    log.error(
                            "Job {} failed: {}",
                            job.jobId(),
                            error.getMessage(),
                            error
                    );

                    return updateJobStatus(job.jobId(), entity -> {
                        entity.setStatus(FAILED);
                        entity.setFinishedAt(LocalDateTime.now());
                        entity.setMessage(error.getMessage());
                    }).onErrorResume(statusError -> {
                        log.error(
                                "Could not mark job {} as FAILED",
                                job.jobId(),
                                statusError
                        );
                        return Mono.empty();
                    });
                });
    }

    private Mono<Void> updateJobStatus(
            String jobId,
            Consumer<ImportJobEntity> modifier
    ) {
        return importJobRepository.findById(jobId)
                                  .switchIfEmpty(Mono.error(
                                          new IllegalStateException(
                                                  "Import job not found: " + jobId
                                          )
                                  ))
                                  .doOnNext(modifier)
                                  .flatMap(importJobRepository::save)
                                  .then();
    }
}