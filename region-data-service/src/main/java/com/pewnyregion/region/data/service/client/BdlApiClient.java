package com.pewnyregion.region.data.service.client;

import com.pewnyregion.region.data.service.model.BdlRawDataResponse;
import com.pewnyregion.region.data.service.model.BdlResponse;
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

    public Mono<BdlRawDataResponse> fetchRawData(
            String unitExternalId,
            List<Integer> variableExternalIds
    ) {
        return bdlWebClient.get()
                           .uri(uriBuilder -> uriBuilder
                                   .path("/data/by-unit/{id}")
                                   .queryParam("var-id", variableExternalIds.toArray())
                                   .queryParam("format", RESPONSE_FORMAT)
                                   .build(unitExternalId))
                           .retrieve()
                           .bodyToMono(BdlRawDataResponse.class);
    }

    public Mono<BdlRawDataResponse> fetchTargetedData(
            String unitExternalId,
            List<Integer> variableExternalIds,
            List<Integer> years
    ) {
        return bdlWebClient.get()
                           .uri(uriBuilder -> uriBuilder
                                   .path("/data/by-unit/{id}")
                                   .queryParam("var-id", variableExternalIds.toArray())
                                   .queryParam("year", years.toArray())
                                   .queryParam("format", RESPONSE_FORMAT)
                                   .build(unitExternalId))
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