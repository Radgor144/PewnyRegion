package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.entity.CountyEntity;
import com.pewnyregion.region.analytics.service.model.AnalyticsResponse;
import com.pewnyregion.region.analytics.service.model.AnalyticsValueDto;
import com.pewnyregion.region.analytics.service.model.BdlRawDataResponse;
import com.pewnyregion.region.analytics.service.model.BdlVarId;
import com.pewnyregion.region.analytics.service.model.ResultGroup;
import com.pewnyregion.region.analytics.service.repository.CountyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final String RESPONSE_FORMAT = "json";

    private final CountyRepository countyRepository;
    private final WebClient bdlWebClient;

    public Mono<List<AnalyticsResponse>> getAnalyticsData(String terytCode, List<BdlVarId> variables) {
        return countyRepository.findByTerytCode(terytCode)
                               .flatMap(county -> fetchBdlRawData(county, variables))
                               .map(this::toAnalyticsResponses)
                               .doOnError(error -> log.error("Error while fetching analytics data", error));
    }

    private Mono<BdlRawDataResponse> fetchBdlRawData(CountyEntity county, List<BdlVarId> variables) {
        List<Integer> requestedVarIds = variables.stream()
                                                 .flatMap(variable -> variable.getVarIds().stream())
                                                 .toList();

        return bdlWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/by-unit/{id}")
                        .queryParam("var-id", requestedVarIds.toArray())
                        .queryParam("format", RESPONSE_FORMAT)
                        .build(county.getId()))
                .retrieve()
                .bodyToMono(BdlRawDataResponse.class);
    }

    private List<AnalyticsResponse> toAnalyticsResponses(BdlRawDataResponse rawResponse) {
        Map<BdlVarId, List<ResultGroup>> resultsByVariable = rawResponse.results().stream()
                .collect(Collectors.groupingBy(result -> BdlVarId.fromVarId(result.id())));

        return resultsByVariable.entrySet().stream()
                .map(entry -> buildAnalyticsResponseForVariable(rawResponse, entry.getKey(), entry.getValue()))
                .toList();
    }

    private AnalyticsResponse buildAnalyticsResponseForVariable(BdlRawDataResponse rawResponse,
                                                                BdlVarId variable,
                                                                List<ResultGroup> resultsForVariable) {

        List<AnalyticsValueDto> valuesSortedByYear = resultsForVariable.stream()
                .flatMap(result -> result.values().stream())
                .sorted(Comparator.comparingInt(AnalyticsValueDto::year))
                .toList();

        return AnalyticsResponse.builder()
                .variableName(variable.getApiName())
                .unitId(rawResponse.unitId())
                .unitName(rawResponse.unitName())
                .values(valuesSortedByYear)
                .build();
    }
}