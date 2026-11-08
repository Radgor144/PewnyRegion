package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.client.BdlApiClient;
import com.pewnyregion.region.data.service.component.BdlRawDataMapper;
import com.pewnyregion.region.data.service.entity.TerritorialUnitExternalIdEntity;
import com.pewnyregion.region.data.service.entity.VariableEntity;
import com.pewnyregion.region.data.service.entity.VariableExternalIdEntity;
import com.pewnyregion.region.data.service.model.BdlRawDataResponse;
import com.pewnyregion.region.data.service.model.StatisticalDataImportRecord;
import com.pewnyregion.region.data.service.model.TargetedImportRequest;
import com.pewnyregion.region.data.service.repository.TerritorialUnitExternalIdRepository;
import com.pewnyregion.region.data.service.repository.TerritorialUnitRepository;
import com.pewnyregion.region.data.service.repository.VariableExternalIdRepository;
import com.pewnyregion.region.data.service.repository.VariableRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BdlStatisticalDataImportService {

    private static final String PROVIDER = "BDL";
    private static final String COUNTY_LEVEL = "COUNTY";

    private static final int CHUNK_SIZE = 10;
    private static final int MAX_RETRIES = 3;
    private static final Duration REQUEST_DELAY = Duration.ofMillis(500);

    private final BdlApiClient bdlApiClient;
    private final BdlRawDataMapper bdlRawDataMapper;
    private final StatisticalDataPersistenceService persistenceService;

    private final TerritorialUnitRepository territorialUnitRepository;
    private final TerritorialUnitExternalIdRepository territorialUnitExternalIdRepository;

    private final VariableRepository variableRepository;
    private final VariableExternalIdRepository variableExternalIdRepository;

    public Mono<Integer> importAllCounties(String importJobId) {
        return validateImportJobId(importJobId)
                .then(Mono.zip(findAllCountyMappings(), findAllBdlVariableMappings()))
                .flatMapMany(result -> Flux.fromIterable(result.getT1())
                                           .concatMap(unitMapping -> importUnitData(unitMapping, result.getT2(), List.of(), importJobId)))
                .reduce(0, Integer::sum)
                .doOnNext(importedRecords -> log.info("BDL county import completed: importedRecords={}", importedRecords));
    }

    public Mono<Integer> importUnit(String terytCode, String importJobId) {
        return validateImportJobId(importJobId)
                .then(Mono.zip(findBdlUnitMapping(terytCode), findAllBdlVariableMappings()))
                .flatMap(result -> importUnitData(result.getT1(), result.getT2(), List.of(), importJobId))
                .doOnNext(importedRecords -> log.info("BDL import completed for TERYT {}: importedRecords={}", terytCode, importedRecords));
    }

    public Mono<Integer> importTargetedCounties(TargetedImportRequest request, String importJobId) {
        List<String> apiNames = request.apiNames().stream()
                                       .distinct()
                                       .toList();

        List<Integer> years = request.years().stream()
                                     .distinct()
                                     .sorted()
                                     .toList();

        return validateImportJobId(importJobId)
                .then(findBdlVariableMappings(apiNames))
                .flatMap(variableMappings ->
                        findAllCountyMappings()
                                .flatMapMany(unitMappings ->
                                        Flux.fromIterable(unitMappings)
                                            .concatMap(unitMapping -> importUnitData(unitMapping, variableMappings, years, importJobId))
                                )
                                .reduce(0, Integer::sum)
                )
                .doOnNext(importedRecords -> log.info("BDL targeted import completed: variables={}, years={}, importedRecords={}", apiNames, years, importedRecords));
    }

    private Mono<List<TerritorialUnitExternalIdEntity>> findAllCountyMappings() {
        return Mono.zip(territorialUnitRepository.countByUnitLevelAndActive(COUNTY_LEVEL, true),
                        territorialUnitExternalIdRepository.findAllByProviderAndUnitLevel(PROVIDER, COUNTY_LEVEL).collectList())
                   .flatMap(result -> {
                       long expectedCount = result.getT1();
                       List<TerritorialUnitExternalIdEntity> mappings = result.getT2();

                       if (expectedCount == 0) {
                           return Mono.error(new IllegalStateException("No active counties found in TERYT catalog"));
                       }

                       if (expectedCount != mappings.size()) {
                           return Mono.error(new IllegalStateException("Incomplete BDL county mappings. Expected: " + expectedCount + ", found: " + mappings.size()));
                       }

                       return Mono.just(mappings);
                   });
    }

    private Mono<TerritorialUnitExternalIdEntity> findBdlUnitMapping(String terytCode) {
        return territorialUnitRepository
                .findByTerytCodeAndUnitLevelAndActive(terytCode, COUNTY_LEVEL, true)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Active county not found for TERYT code: " + terytCode
                )))
                .flatMap(unit ->
                        territorialUnitExternalIdRepository
                                .findAllByTerritorialUnitId(unit.getId())
                                .filter(mapping -> PROVIDER.equals(mapping.getProvider()))
                                .singleOrEmpty()
                                .switchIfEmpty(Mono.error(new IllegalStateException("BDL mapping not found for TERYT code: " + terytCode)))
                );
    }

    private Mono<List<VariableExternalIdEntity>> findAllBdlVariableMappings() {
        return variableExternalIdRepository.findAllByProvider(PROVIDER)
                                           .collectList()
                                           .filter(mappings -> !mappings.isEmpty())
                                           .switchIfEmpty(Mono.error(new IllegalStateException("No BDL variable mappings found")));
    }

    private Mono<List<VariableExternalIdEntity>> findBdlVariableMappings(List<String> apiNames) {
        return variableRepository.findByApiNameIn(apiNames)
                                 .collectList()
                                 .flatMap(variables -> {
                                     Set<String> foundApiNames = variables.stream()
                                                                          .map(VariableEntity::getApiName)
                                                                          .collect(Collectors.toSet());

                                     List<String> missingApiNames = apiNames.stream()
                                                                            .filter(apiName -> !foundApiNames.contains(apiName))
                                                                            .toList();

                                     if (!missingApiNames.isEmpty()) {
                                         return Mono.error(new IllegalArgumentException("Unknown variables: " + missingApiNames));
                                     }

                                     List<Integer> variableIds = variables.stream()
                                                                          .map(VariableEntity::getId)
                                                                          .toList();

                                     return variableExternalIdRepository
                                             .findAllByVariableIdInAndProvider(variableIds, PROVIDER)
                                             .collectList()
                                             .flatMap(mappings -> validateVariableMappings(variables, mappings));
                                 });
    }

    private Mono<List<VariableExternalIdEntity>> validateVariableMappings(List<VariableEntity> variables, List<VariableExternalIdEntity> mappings) {
        Set<Integer> mappedVariableIds = mappings.stream()
                                                 .map(VariableExternalIdEntity::getVariableId)
                                                 .collect(Collectors.toSet());

        List<String> missingMappings = variables.stream()
                                                .filter(variable -> !mappedVariableIds.contains(variable.getId()))
                                                .map(VariableEntity::getApiName)
                                                .toList();

        if (!missingMappings.isEmpty()) {
            return Mono.error(new IllegalStateException("Missing BDL mappings for variables: " + missingMappings));
        }

        return Mono.just(mappings);
    }

    private Mono<Integer> importUnitData(TerritorialUnitExternalIdEntity unitMapping,
                                         List<VariableExternalIdEntity> variableMappings,
                                         List<Integer> years,
                                         String importJobId) {
        return Flux.fromIterable(variableMappings)
                   .buffer(CHUNK_SIZE)
                   .concatMap(chunk ->
                           Mono.delay(REQUEST_DELAY)
                               .then(importChunk(unitMapping, chunk, years, importJobId)))
                   .reduce(0, Integer::sum);
    }

    private Mono<Integer> importChunk(TerritorialUnitExternalIdEntity unitMapping,
                                      List<VariableExternalIdEntity> variableMappings,
                                      List<Integer> years,
                                      String importJobId) {
        List<Integer> externalVariableIds = variableMappings.stream()
                                                            .map(mapping -> parseExternalId(mapping.getExternalId()))
                                                            .toList();

        Mono<BdlRawDataResponse> request = years.isEmpty() ? bdlApiClient.fetchRawData(unitMapping.getExternalId(), externalVariableIds)
                                                           : bdlApiClient.fetchTargetedData(unitMapping.getExternalId(), externalVariableIds, years);

        return request
                .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofSeconds(1))
                                .filter(this::isRetryable)
                                .doBeforeRetry(retry ->
                                        log.warn("BDL API retry {}/{} for unit {}", retry.totalRetries() + 1, MAX_RETRIES, unitMapping.getExternalId())))
                .flatMap(response -> persistResponse(unitMapping, variableMappings, response, importJobId));
    }

    private Mono<Integer> persistResponse(TerritorialUnitExternalIdEntity unitMapping,
                                          List<VariableExternalIdEntity> variableMappings,
                                          BdlRawDataResponse response,
                                          String importJobId) {
        List<StatisticalDataImportRecord> records = bdlRawDataMapper.map(unitMapping, variableMappings, response);

        return persistenceService.saveAll(importJobId, Flux.fromIterable(records));
    }

    private Mono<Void> validateImportJobId(String importJobId) {
        return Mono.justOrEmpty(importJobId)
                   .filter(id -> !id.isBlank())
                   .switchIfEmpty(Mono.error(new IllegalArgumentException("Import job ID is required")))
                   .then();
    }

    private Integer parseExternalId(String externalId) {
        try {
            return Integer.valueOf(externalId);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid BDL variable ID: " + externalId, exception);
        }
    }

    private boolean isRetryable(Throwable throwable) {
        return throwable instanceof WebClientRequestException || throwable instanceof WebClientResponseException exception
                && (exception.getStatusCode().value() == 429 || exception.getStatusCode().is5xxServerError());
    }
}