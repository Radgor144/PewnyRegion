package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.model.exception.MapValidationException;
import com.pewnyregion.region.analytics.service.model.MapCountyScoreDto;
import com.pewnyregion.region.analytics.service.model.MapResponse;
import com.pewnyregion.region.analytics.service.repository.MapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {

    private final MapRepository mapRepository;

    public Flux<MapResponse> getMapData(List<String> apiNames, Integer yearFrom, Integer yearTo) {
        return getValidatedUniqueNames(apiNames)
                .flatMapMany(validNames -> mapRepository.getMapData(validNames, yearFrom, yearTo))
                .map(this::mapToResponse);
    }

    private Mono<List<String>> getValidatedUniqueNames(List<String> requestedNames) {
        List<String> uniqueNames = requestedNames.stream().distinct().toList();

        return mapRepository.findExistingApiNames(uniqueNames)
                            .collectList()
                            .flatMap(existingNames -> {
                                if (existingNames.size() == uniqueNames.size()) {
                                    return Mono.just(uniqueNames);
                                }

                                List<String> missing = uniqueNames.stream()
                                                                  .filter(name -> !existingNames.contains(name))
                                                                  .toList();

                                return Mono.error(new MapValidationException("Invalid apiNames provided. Not found: " + missing));
                            });
    }

    private MapResponse mapToResponse(MapCountyScoreDto dto) {
        return new MapResponse(dto.countyId(), dto.countyName(), dto.score());
    }
}