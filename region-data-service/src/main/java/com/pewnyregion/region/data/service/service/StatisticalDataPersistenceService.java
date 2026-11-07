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

    public Mono<Integer> saveAll(Flux<StatisticalDataImportRecord> records) {
        return Mono.defer(() -> {
            LocalDateTime importedAt = LocalDateTime.now();

            return records
                    .concatMap(record -> recordRepository.insertRecord(
                            record.territorialUnitExternalIdId(),
                            record.variableExternalIdId(),
                            record.year(),
                            record.value(),
                            importedAt
                    ))
                    .reduce(0, Integer::sum);
        });
    }
}