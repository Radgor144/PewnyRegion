package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.entity.BdlDataRecordEntity;
import com.pewnyregion.region.analytics.service.model.BdlRawDataResponse;
import com.pewnyregion.region.analytics.service.repository.BdlDataRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BdlDataPersistenceService {

    private final BdlDataRecordRepository recordRepository;

    public Mono<BdlRawDataResponse> saveImportedData(String countyId, BdlRawDataResponse response) {
        return Mono.justOrEmpty(response)
                .filter(this::hasResults)
                .flatMap(res -> {
                    List<BdlDataRecordEntity> entities = mapToEntities(countyId, res);
                    return recordRepository.saveAll(entities)
                            .then()
                            .thenReturn(res);
                })
                .defaultIfEmpty(response);
    }

    private boolean hasResults(BdlRawDataResponse response) {
        return !CollectionUtils.isEmpty(response.results());
    }

    private List<BdlDataRecordEntity> mapToEntities(String countyId, BdlRawDataResponse response) {
        LocalDateTime importTime = LocalDateTime.now();

        return response.results().stream()
                .flatMap(result -> result.values().stream()
                        .map(valueDto -> BdlDataRecordEntity.builder()
                                .countyId(countyId)
                                .variableId(result.id())
                                .year(valueDto.year())
                                .value(valueDto.val())
                                .importedAt(importTime)
                                .build()))
                .toList();
    }
}