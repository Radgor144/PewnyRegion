package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.model.StatisticalDataImportRecord;
import com.pewnyregion.region.data.service.repository.StatisticalDataRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatisticalDataPersistenceService {

    private final StatisticalDataRecordRepository recordRepository;

    public Mono<Integer> saveAll(String importJobId, Flux<StatisticalDataImportRecord> records) {
        return Mono.defer(() -> {
            if (importJobId == null || importJobId.isBlank()) {
                return Mono.error(new IllegalArgumentException("Import job ID is required"));
            }

            LocalDateTime importedAt = LocalDateTime.now();

            return records.concatMap(record -> recordRepository.insertRecord(record.territorialUnitExternalIdId(),
                                                                             record.variableExternalIdId(),
                                                                             record.year(),
                                                                             record.value(),
                                                                             importJobId,
                                                                             importedAt))
                          .reduce(0, Integer::sum);
        });
    }
}