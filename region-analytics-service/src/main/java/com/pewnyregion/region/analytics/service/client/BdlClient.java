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
public class BdlClient {
    private static final String RESPONSE_FORMAT = "json";
    public static final int PAGE_SIZE = 100;

    private final WebClient bdlWebClient;

    public Mono<BdlRawDataResponse> fetchMetrics(String unitId, List<Integer> varIds) {
        return bdlWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/by-unit/{id}")
                        .queryParam("var-id", varIds.toArray())
                        .queryParam("format", RESPONSE_FORMAT)
                        .build(unitId))
                .retrieve()
                .bodyToMono(BdlRawDataResponse.class);
    }

    public Mono<BdlResponse> fetchPage(int level, int page) {
        return bdlWebClient.get()
                .uri(uri -> uri
                        .path("/Units")
                        .queryParam("level", level)
                        .queryParam("format", RESPONSE_FORMAT)
                        .queryParam("page", page)
                        .queryParam("page-size", PAGE_SIZE)
                        .build())
                .retrieve()
                .bodyToMono(BdlResponse.class);
    }
}
