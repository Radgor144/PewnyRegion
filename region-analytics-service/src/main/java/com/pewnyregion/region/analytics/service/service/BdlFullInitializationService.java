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
    private final NormalizationService normalizationService;

    public Mono<Void> runFullInitialization() {
        return dataRecordRepository.count()
                                   .flatMapMany(count -> {
                                       if (count > 0) {
                                           return Flux.error(new IllegalStateException("Database contains existing records"));
                                       }

                                       log.info("Starting full initialization");
                                       return variableService.getAllVariableIds();
                                   })
                                   .flatMap(this::buildInitChunks)
                                   .delayElements(REQUEST_DELAY)
                                   .flatMap(this::executeInitTask, 1)
                                   .then()
                                   .doOnSuccess(v -> log.info("Full initialization finished"))
                                   .then(runNormalizationAfterImport());
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

    private Mono<Void> runNormalizationAfterImport() {
        return Mono.defer(() -> {
                       log.info("Starting normalization after full import completion");
                       return normalizationService.calculateAndSaveScoresForAllYears();
                   })
                   .doOnSuccess(v -> log.info("Normalization completed"));
    }
}