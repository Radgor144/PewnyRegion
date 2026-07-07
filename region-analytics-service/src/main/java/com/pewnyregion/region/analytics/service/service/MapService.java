package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.component.MapValidator;
import com.pewnyregion.region.analytics.service.model.dto.MapCountyScoreDto;
import com.pewnyregion.region.analytics.service.model.MapRequest;
import com.pewnyregion.region.analytics.service.model.MapResponse;
import com.pewnyregion.region.analytics.service.repository.MapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class MapService {

    private final MapRepository mapRepository;
    private final MapValidator mapValidator;

    public Flux<MapResponse> getMapData(MapRequest request) {
        return mapValidator.validate(request.apiNames())
                           .thenMany(fetchMapData(request))
                           .map(this::mapToResponse);
    }

    private Flux<MapCountyScoreDto> fetchMapData(MapRequest request) {
        return mapRepository.getMapData(request.apiNames(),
                                        request.yearFrom(),
                                        request.yearTo());
    }

    private MapResponse mapToResponse(MapCountyScoreDto dto) {
        return new MapResponse(dto.countyId(), dto.countyName(), dto.score());
    }
}