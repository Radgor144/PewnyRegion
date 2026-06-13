package com.pewnyregion.region.analytics.service.client;

import com.pewnyregion.region.analytics.service.model.BdlRawDataResponse;
import com.pewnyregion.region.analytics.service.model.BdlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BdlApiClient {

    private static final String RESPONSE_FORMAT = "json";
    private final WebClient bdlWebClient;

    public Mono<BdlRawDataResponse> fetchRawData(String countyId, List<Integer> varIds) {
        return bdlWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/by-unit/{id}")
                        .queryParam("var-id", varIds.toArray())
                        .queryParam("format", RESPONSE_FORMAT)
                        .build(countyId))
                .retrieve()
                .bodyToMono(BdlRawDataResponse.class);
    }

    public Mono<BdlRawDataResponse> fetchTargetedData(String countyId, List<Integer> varIds, List<Integer> years) {
        return bdlWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/by-unit/{id}")
                        .queryParam("var-id", varIds.toArray())
                        .queryParam("year", years.toArray())
                        .queryParam("format", RESPONSE_FORMAT)
                        .build(countyId))
                .retrieve()
                .bodyToMono(BdlRawDataResponse.class);
    }

    public Mono<BdlResponse> fetchUnits(int page, int level, int pageSize) {
        return bdlWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/Units")
                        .queryParam("level", level)
                        .queryParam("format", RESPONSE_FORMAT)
                        .queryParam("page", page)
                        .queryParam("page-size", pageSize)
                        .build())
                .retrieve()
                .bodyToMono(BdlResponse.class);
    }
}