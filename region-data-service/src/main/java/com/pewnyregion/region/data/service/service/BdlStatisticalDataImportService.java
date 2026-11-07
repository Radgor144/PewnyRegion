package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.client.BdlApiClient;
import com.pewnyregion.region.data.service.component.BdlRawDataMapper;
import com.pewnyregion.region.data.service.entity.TerritorialUnitExternalIdEntity;
import com.pewnyregion.region.data.service.entity.VariableExternalIdEntity;
import com.pewnyregion.region.data.service.model.StatisticalDataImportRecord;
import com.pewnyregion.region.data.service.repository.TerritorialUnitExternalIdRepository;
import com.pewnyregion.region.data.service.repository.TerritorialUnitRepository;
import com.pewnyregion.region.data.service.repository.VariableExternalIdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BdlStatisticalDataImportService {

    private static final String PROVIDER = "BDL";
    private static final int CHUNK_SIZE = 10;
    private static final Duration REQUEST_DELAY = Duration.ofMillis(300);
    private static final int MAX_RETRIES = 3;

    private final BdlApiClient bdlApiClient;
    private final BdlRawDataMapper bdlRawDataMapper;
    private final StatisticalDataPersistenceService persistenceService;
    private final TerritorialUnitRepository territorialUnitRepository;
    private final TerritorialUnitExternalIdRepository territorialUnitExternalIdRepository;
    private final VariableExternalIdRepository variableExternalIdRepository;

    public Mono<Integer> importUnit(String terytCode) {
        return territorialUnitRepository.findByTerytCode(terytCode)
                                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                                "Territorial unit not found: " + terytCode
                                        )))
                                        .flatMap(unit -> territorialUnitExternalIdRepository
                                                .findAllByTerritorialUnitId(unit.getId())
                                                .filter(mapping -> PROVIDER.equals(mapping.getProvider()))
                                                .singleOrEmpty()
                                                .switchIfEmpty(Mono.error(new IllegalStateException(
                                                        "BDL mapping not found for TERYT: " + terytCode
                                                )))
                                                .flatMap(unitMapping -> importUnitData(unitMapping, terytCode))
                                        );
    }

    private Mono<Integer> importUnitData(
            TerritorialUnitExternalIdEntity unitMapping,
            String terytCode
    ) {
        return variableExternalIdRepository.findAllByProvider(PROVIDER)
                                           .collectList()
                                           .filter(mappings -> !mappings.isEmpty())
                                           .switchIfEmpty(Mono.error(new IllegalStateException(
                                                   "No BDL variable mappings found"
                                           )))
                                           .flatMap(mappings -> Flux.fromIterable(mappings)
                                                                    .buffer(CHUNK_SIZE)
                                                                    .concatMap(chunk -> Mono.delay(REQUEST_DELAY)
                                                                                            .then(importChunk(unitMapping, chunk)))
                                                                    .reduce(0, Integer::sum)
                                           )
                                           .doOnNext(importedRecords -> log.info(
                                                   "BDL import completed for TERYT {}: importedRecords={}",
                                                   terytCode,
                                                   importedRecords
                                           ));
    }

    private Mono<Integer> importChunk(
            TerritorialUnitExternalIdEntity unitMapping,
            List<VariableExternalIdEntity> variableMappings
    ) {
        List<Integer> variableIds = variableMappings.stream()
                                                    .map(mapping -> parseVariableId(mapping.getExternalId()))
                                                    .toList();

        return bdlApiClient.fetchRawData(unitMapping.getExternalId(), variableIds)
                           .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofSeconds(1))
                                           .filter(this::isRetryable)
                                           .doBeforeRetry(retry -> log.warn(
                                                   "BDL API retry {}/{} for unit {}",
                                                   retry.totalRetries() + 1,
                                                   MAX_RETRIES,
                                                   unitMapping.getExternalId()
                                           )))
                           .flatMap(response -> {
                               List<StatisticalDataImportRecord> records =
                                       bdlRawDataMapper.map(unitMapping, variableMappings, response);

                               return persistenceService.saveAll(Flux.fromIterable(records));
                           });
    }

    private Integer parseVariableId(String externalId) {
        try {
            return Integer.valueOf(externalId);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid BDL variable ID: " + externalId,
                    exception
            );
        }
    }

    private boolean isRetryable(Throwable throwable) {
        return throwable instanceof WebClientResponseException exception
                && (exception.getStatusCode().value() == 429
                || exception.getStatusCode().is5xxServerError());
    }
}