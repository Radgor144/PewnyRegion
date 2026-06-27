package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.model.MapResponse;
import com.pewnyregion.region.analytics.service.repository.MapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {

    private final MapRepository mapRepository;

    public Flux<MapResponse> getMapData(List<String> apiNames, Integer yearFrom, Integer yearTo) {

        validate(apiNames, yearFrom, yearTo);

        return mapRepository.getMapData(apiNames, yearFrom, yearTo)
                            .map(dto -> new MapResponse(
                                    dto.getCountyId(),
                                    dto.getCountyName(),
                                    normalize(dto.getScore())
                            ));
    }

    private void validate(List<String> apiNames, Integer yearFrom, Integer yearTo) {
        if (apiNames == null || apiNames.isEmpty()) {
            throw new IllegalArgumentException("apiNames cannot be empty");
        }

        if (apiNames.size() > 5) {
            throw new IllegalArgumentException("Maximum 5 variables allowed");
        }

        if (yearFrom == null || yearTo == null || yearFrom > yearTo) {
            throw new IllegalArgumentException("Invalid year range");
        }
    }

    private Double normalize(Double score) {
        if (score == null) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(100.0, Math.round(score * 100.0) / 100.0));
    }
}