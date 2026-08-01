package com.pewnyregion.region.analytics.service.component;

import com.pewnyregion.region.analytics.service.entity.ImportJobEntity;
import com.pewnyregion.region.analytics.service.model.PendingJob;
import com.pewnyregion.region.analytics.service.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Consumer;

import static com.pewnyregion.region.analytics.service.model.consts.ImportJobStatus.COMPLETED;
import static com.pewnyregion.region.analytics.service.model.consts.ImportJobStatus.FAILED;
import static com.pewnyregion.region.analytics.service.model.consts.ImportJobStatus.RUNNING;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackgroundJobQueue implements DisposableBean {

    private final ImportJobRepository importJobRepository;
    private final Sinks.Many<PendingJob> sink = Sinks.many().unicast().onBackpressureBuffer();
    private Disposable subscriptionDisposable;

    public void enqueueJob(String jobId, Mono<String> task) {
        Sinks.EmitResult emitResult = sink.tryEmitNext(new PendingJob(jobId, task));
        if (!Objects.equals(emitResult, Sinks.EmitResult.OK)) {
            log.error("Error enqueueing job {}, emitResult {}", jobId, emitResult);
        } else {
            log.info("Job {} enqueued successfully", jobId);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startJobProcessor() {
        log.info("Starting job processor");

        subscriptionDisposable = sink.asFlux()
                                     .flatMap(this::processJob, 1)
                                     .subscribeOn(Schedulers.boundedElastic())
                                     .subscribe(
                                             result -> log.debug("job processing completed with signal"),
                                             error -> log.error("Fatal error in job stream: {}", error.getMessage(), error),
                                             () -> log.warn("Job stream completed unexpectedly")
                                     );
    }

    @Override
    public void destroy() {
        if (subscriptionDisposable != null && !subscriptionDisposable.isDisposed()) {
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
                .then(job.task())
                .flatMap(message -> updateJobStatus(job.jobId(), entity -> {
                    entity.setStatus(COMPLETED);
                    entity.setFinishedAt(LocalDateTime.now());
                    entity.setMessage(message);
                }))
                .onErrorResume(error -> {
                    log.error("Error processing job {} failed: {}", job.jobId(), error.getMessage(), error);
                    return updateJobStatus(job.jobId(), entity -> {
                        entity.setStatus(FAILED);
                        entity.setFinishedAt(LocalDateTime.now());
                        entity.setMessage(error.getMessage());
                    });
                });
    }

    private Mono<Void> updateJobStatus(String jobId, Consumer<ImportJobEntity> modifier) {
        return importJobRepository.findById(jobId)
                                  .doOnNext(modifier)
                                  .then();
    }
}
