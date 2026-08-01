package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.client.BdlApiClient;
import com.pewnyregion.region.analytics.service.model.BdlRawDataResponse;
import com.pewnyregion.region.analytics.service.model.ImportChunk;
import com.pewnyregion.region.analytics.service.model.ImportSummary;
import com.pewnyregion.region.analytics.service.repository.CountyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BdlDataImportService {

    private static final int CHUNK_SIZE = 10;
    private static final Duration REQUEST_DELAY = Duration.ofMillis(150);
    private static final int MAX_RETRIES = 3;

    private final VariableService variableService;
    private final BdlApiClient bdlApiClient;
    private final BdlDataPersistenceService persistenceService;
    private final CountyRepository countyRepository;

    public Mono<ImportSummary> runImport(List<String> apiNames, List<Integer> years) {
        String mode = CollectionUtils.isEmpty(years) ? "FULL" : "TARGETED";
        log.info("Starting process: {}", mode);

        return resolveVariableIds(apiNames)
                .flatMapMany(varIds -> buildChunks(varIds, years))
                .concatMap(task -> Mono.delay(REQUEST_DELAY).then(executeTask(task)))
                .count()
                .map((count) -> new ImportSummary(count.intValue()))
                .doOnSuccess(s -> log.info("Import completed - {}", s.toMessage()));
    }

    private Mono<List<Integer>> resolveVariableIds(List<String> apiNames) {
        if (CollectionUtils.isEmpty(apiNames)) {
            return variableService.getAllVariableIds();
        }
        return variableService.getVariableIdsByApiNames(apiNames);
    }

    private Mono<BdlRawDataResponse> executeTask(ImportChunk task) {
        log.info("Processing county: {}, variables: {}, targeted: {}", task.county().getName(), task.varIds().size(), task.isTargeted());

        Mono<BdlRawDataResponse> apiCall = task.isTargeted()
                ? bdlApiClient.fetchTargetedData(task.county().getId(), task.varIds(), task.years())
                : bdlApiClient.fetchRawData(task.county().getId(), task.varIds());

        return apiCall
                .retryWhen(createRetrySpec(task.county().getName()))
                .flatMap(response -> persistenceService.saveImportedData(task.county().getId(), response))
                .doOnNext(res -> log.info("Saved data for county: {}", task.county().getName()))
                .onErrorResume(e -> {
                    log.error("Failure for county: {}, due to: {}", task.county().getName(), e.getMessage());
                    return Mono.empty();
                });
    }

    private Flux<ImportChunk> buildChunks(List<Integer> varIds, List<Integer> years) {
        return countyRepository.findAll()
                               .concatMap(county -> Flux.fromIterable(varIds)
                                                        .buffer(CHUNK_SIZE)
                                                        .map(chunk -> new ImportChunk(county, chunk, years)));
    }

    private Retry createRetrySpec(String countyName) {
        return Retry.backoff(MAX_RETRIES, Duration.ofSeconds(1))
                    .doBeforeRetry(retry -> log.warn("BDL API retry {}/{} for {}", retry.totalRetries() + 1, MAX_RETRIES, countyName));
    }
}
