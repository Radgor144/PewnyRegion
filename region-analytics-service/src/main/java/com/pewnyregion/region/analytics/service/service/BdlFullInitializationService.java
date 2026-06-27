package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.client.BdlApiClient;
import com.pewnyregion.region.analytics.service.model.BdlRawDataResponse;
import com.pewnyregion.region.analytics.service.model.InitChunk;
import com.pewnyregion.region.analytics.service.repository.BdlDataRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BdlFullInitializationService {

    private static final int CHUNK_SIZE = 10;
    private static final Duration REQUEST_DELAY = Duration.ofMillis(2000);
    private static final int MAX_RETRIES = 3;

    private final VariableService variableService;
    private final CountyReactiveProvider countyProvider;
    private final BdlApiClient bdlApiClient;
    private final BdlDataPersistenceService persistenceService;
    private final BdlDataRecordRepository dataRecordRepository;

    public Flux<BdlRawDataResponse> runFullInitialization() {
        return dataRecordRepository.count()
                .filter(count -> count == 0)
                .switchIfEmpty(Mono.error(new IllegalStateException("Database contains existing records. Full initialization aborted.")))
                .flatMap(ignored -> variableService.getAllVariableIds())
                .flatMapMany(this::buildInitChunks)
                .delayElements(REQUEST_DELAY)
                .flatMap(this::executeInitTask, 1);
    }

    private Flux<InitChunk> buildInitChunks(List<Integer> varIds) {
        return countyProvider.streamAllCounties()
                .flatMap(county -> Flux.fromIterable(varIds)
                        .buffer(CHUNK_SIZE)
                        .map(chunk -> new InitChunk(county, chunk)));
    }

    private Mono<BdlRawDataResponse> executeInitTask(InitChunk task) {
        log.info("[INIT] Requesting BDL data for county: {}, variables: {}", task.county().getName(), task.varIds());
        return bdlApiClient.fetchRawData(task.county().getId(), task.varIds())
                .retryWhen(createRetrySpec(task.county().getName()))
                .flatMap(response -> persistenceService.saveImportedData(task.county().getId(), response))
                .doOnNext(res -> log.info("[INIT] Successfully saved initial data for county: {}", task.county().getName()))
                .onErrorResume(e -> {
                    log.error("[INIT] Failure for county: {} due to: {}", task.county().getName(), e.getMessage());
                    return Mono.empty();
                });
    }

    private Retry createRetrySpec(String countyName) {
        return Retry.backoff(MAX_RETRIES, Duration.ofSeconds(1))
                .doBeforeRetry(retry -> log.warn("BDL API retry {}/{} for: {}", retry.totalRetries() + 1, MAX_RETRIES, countyName));
    }
}