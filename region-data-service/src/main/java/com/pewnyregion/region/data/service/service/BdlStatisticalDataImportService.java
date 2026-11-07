
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
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BdlStatisticalDataImportService {

    private static final String PROVIDER = "BDL";
    private static final String COUNTY_LEVEL = "COUNTY";
    private static final int CHUNK_SIZE = 10;
    private static final Duration REQUEST_DELAY = Duration.ofMillis(500);
    private static final int MAX_RETRIES = 3;

    private final BdlApiClient bdlApiClient;
    private final BdlRawDataMapper bdlRawDataMapper;
    private final StatisticalDataPersistenceService persistenceService;
    private final TerritorialUnitRepository territorialUnitRepository;
    private final TerritorialUnitExternalIdRepository territorialUnitExternalIdRepository;
    private final VariableExternalIdRepository variableExternalIdRepository;

    public Mono<Integer> importUnit(String terytCode, String importJobId) {
        return validateImportJobId(importJobId)
                .then(Mono.zip(
                        findBdlUnitMapping(terytCode),
                        findBdlVariableMappings()
                ))
                .flatMap(result ->
                        importUnitData(result.getT1(), result.getT2(), importJobId)
                )
                .doOnNext(importedRecords ->
                        log.info(
                                "BDL import completed for TERYT {}: importedRecords={}",
                                terytCode,
                                importedRecords
                        )
                );
    }


    public Mono<Integer> importAllCounties(String importJobId) {
        return validateImportJobId(importJobId)
                .then(findBdlVariableMappings())
                .flatMapMany(variableMappings ->
                        territorialUnitExternalIdRepository
                                .findAllByProviderAndUnitLevel(PROVIDER, COUNTY_LEVEL)
                                .concatMap(unitMapping ->
                                        importUnitData(unitMapping, variableMappings, importJobId)
                                )
                                .switchIfEmpty(Flux.error(
                                        new IllegalStateException(
                                                "No BDL county mappings found"
                                        )
                                ))
                )
                .reduce(0, Integer::sum)
                .doOnNext(importedRecords ->
                        log.info(
                                "BDL county import completed: importedRecords={}",
                                importedRecords
                        )
                );
    }


    private Mono<TerritorialUnitExternalIdEntity> findBdlUnitMapping(String terytCode) {
        return territorialUnitRepository.findByTerytCode(terytCode)
                                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                                "Territorial unit not found: " + terytCode
                                        )))
                                        .flatMap(unit ->
                                                territorialUnitExternalIdRepository
                                                        .findAllByTerritorialUnitId(unit.getId())
                                                        .filter(mapping -> PROVIDER.equals(mapping.getProvider()))
                                                        .singleOrEmpty()
                                                        .switchIfEmpty(Mono.error(new IllegalStateException(
                                                                "BDL mapping not found for TERYT: " + terytCode
                                                        )))
                                        );
    }

    private Mono<List<VariableExternalIdEntity>> findBdlVariableMappings() {
        return variableExternalIdRepository.findAllByProvider(PROVIDER)
                                           .collectList()
                                           .filter(mappings -> !mappings.isEmpty())
                                           .switchIfEmpty(Mono.error(new IllegalStateException(
                                                   "No BDL variable mappings found"
                                           )));
    }

    private Mono<Integer> importUnitData(
            TerritorialUnitExternalIdEntity unitMapping,
            List<VariableExternalIdEntity> variableMappings,
            String importJobId
    ) {
        return Flux.fromIterable(variableMappings)
                   .buffer(CHUNK_SIZE)
                   .concatMap(chunk ->
                           Mono.delay(REQUEST_DELAY)
                               .then(importChunk(unitMapping, chunk, importJobId))
                   )
                   .reduce(0, Integer::sum);
    }

    private Mono<Integer> importChunk(
            TerritorialUnitExternalIdEntity unitMapping,
            List<VariableExternalIdEntity> variableMappings,
            String importJobId
    ) {
        List<Integer> variableIds = variableMappings.stream()
                                                    .map(mapping -> parseVariableId(mapping.getExternalId()))
                                                    .toList();

        return bdlApiClient.fetchRawData(unitMapping.getExternalId(), variableIds)
                           .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofSeconds(1))
                                           .filter(this::isRetryable)
                                           .doBeforeRetry(retry ->
                                                   log.warn(
                                                           "BDL API retry {}/{} for unit {} due to {}",
                                                           retry.totalRetries() + 1,
                                                           MAX_RETRIES,
                                                           unitMapping.getExternalId(),
                                                           retry.failure().getClass().getSimpleName()
                                                   )
                                           )
                           )
                           .flatMap(response -> {
                               List<StatisticalDataImportRecord> records =
                                       bdlRawDataMapper.map(unitMapping, variableMappings, response);

                               return persistenceService.saveAll(
                                       importJobId,
                                       Flux.fromIterable(records)
                               );
                           });
    }

    private Mono<Void> validateImportJobId(String importJobId) {
        return Mono.justOrEmpty(importJobId)
                   .filter(id -> !id.isBlank())
                   .switchIfEmpty(Mono.error(
                           new IllegalArgumentException("Import job ID is required")
                   ))
                   .then();
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
        return throwable instanceof WebClientRequestException
                || throwable instanceof WebClientResponseException exception
                && (exception.getStatusCode().value() == 429
                || exception.getStatusCode().is5xxServerError());
    }
}
