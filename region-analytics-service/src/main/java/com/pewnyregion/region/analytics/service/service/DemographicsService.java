package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.entity.CountyEntity;
import com.pewnyregion.region.analytics.service.model.PopulationResponse;
import com.pewnyregion.region.analytics.service.repository.CountyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemographicsService {

    public static final String POPULATION_IN_THOUSANDS_ID = "1645341";
    public static final String FORMAT = "json";

    private final CountyRepository countyRepository;
    private final WebClient bdlWebClient;

    public Mono<PopulationResponse> getPopulationByTeryt(String teryt) {
        return countyRepository.findByTeryt(teryt)
                .flatMap(this::fetchPopulationFromBdl)
                .doOnError(e -> log.error(e.getMessage()));
    }

    private Mono<PopulationResponse> fetchPopulationFromBdl(CountyEntity entity) {
        return bdlWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/by-unit/" + entity.getId())
                        .queryParam("var-id", POPULATION_IN_THOUSANDS_ID)
                        .queryParam("format", FORMAT)
                        .build())
                .retrieve()
                .bodyToMono(PopulationResponse.class);
    }
}