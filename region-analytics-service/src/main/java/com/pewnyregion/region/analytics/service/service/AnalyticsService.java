package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.client.BdlClient;
import com.pewnyregion.region.analytics.service.entity.CountyEntity;
import com.pewnyregion.region.analytics.service.model.AnalyticsResponse;
import com.pewnyregion.region.analytics.service.model.AnalyticsValueDto;
import com.pewnyregion.region.analytics.service.model.BdlRawDataResponse;
import com.pewnyregion.region.analytics.service.model.BdlVariableDto;
import com.pewnyregion.region.analytics.service.model.ResultGroup;
import com.pewnyregion.region.analytics.service.repository.CountyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CountyRepository countyRepository;
    private final BdlVariableService bdlVariableService;
    private final BdlClient bdlClient;

    public Mono<List<AnalyticsResponse>> getAnalyticsData(String terytCode, List<String> apiNames) {
        Mono<CountyEntity> countyMono = countyRepository.findByTerytCode(terytCode);
        Mono<List<BdlVariableDto>> variablesConfigMono = bdlVariableService.getVariablesByApiNames(apiNames).collectList();

        return Mono.zip(countyMono, variablesConfigMono)
                .flatMap(tuple -> fetchBdlRawData(tuple.getT1(), tuple.getT2())
                        .map(rawResponse -> toAnalyticsResponses(rawResponse, tuple.getT2())))
                .doOnError(error -> log.error("Error while fetching analytics data", error));
    }

    private Mono<BdlRawDataResponse> fetchBdlRawData(CountyEntity county, List<BdlVariableDto> variables) {
        List<Integer> requestedVarIds = variables.stream()
                                                 .flatMap(variable -> variable.getVarIds().stream())
                                                 .toList();
        return bdlClient.fetchMetrics(county.getId(), requestedVarIds);
    }

    private List<AnalyticsResponse> toAnalyticsResponses(BdlRawDataResponse rawResponse, List<BdlVariableDto> variablesConfig) {
        Map<String, List<ResultGroup>> resultsByVariable = rawResponse.results().stream()
                .collect(Collectors.groupingBy(result -> findApiNameByVarId(result.id(), variablesConfig)));

        return resultsByVariable.entrySet().stream()
                .map(entry -> buildAnalyticsResponseForVariable(rawResponse, entry.getKey(), entry.getValue()))
                .toList();
    }

    private String findApiNameByVarId(int varId, List<BdlVariableDto> variablesConfig) {
        return variablesConfig.stream()
                .filter(v -> v.getVarIds().contains(varId))
                .map(BdlVariableDto::getApiName)
                .findFirst()
                .orElse("unknown_variable");
    }

    private AnalyticsResponse buildAnalyticsResponseForVariable(BdlRawDataResponse rawResponse,
                                                                String apiName,
                                                                List<ResultGroup> resultsForVariable) {

        List<AnalyticsValueDto> valuesSortedByYear = resultsForVariable.stream()
                .flatMap(result -> result.values().stream())
                .sorted(Comparator.comparingInt(AnalyticsValueDto::year))
                .toList();

        return AnalyticsResponse.builder()
                .variableName(apiName)
                .unitId(rawResponse.unitId())
                .unitName(rawResponse.unitName())
                .values(valuesSortedByYear)
                .build();
    }
}