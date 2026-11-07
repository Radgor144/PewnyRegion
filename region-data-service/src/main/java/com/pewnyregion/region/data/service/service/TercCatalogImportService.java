package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.component.TercRecordMapper;
import com.pewnyregion.region.data.service.component.TercXmlReader;
import com.pewnyregion.region.data.service.model.TercXmlDocument;
import com.pewnyregion.region.data.service.model.TerritorialUnitImportRecord;
import com.pewnyregion.region.data.service.repository.TerritorialUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TercCatalogImportService {

    private final TercXmlReader tercXmlReader;
    private final TercRecordMapper tercRecordMapper;
    private final TerritorialUnitRepository territorialUnitRepository;

    @Value("${teryt.catalog.path}")
    private String catalogPath;

    public Mono<Void> importCatalog() {
        return Mono.fromCallable(() -> tercXmlReader.read(Path.of(catalogPath)))
                   .subscribeOn(Schedulers.boundedElastic())
                   .flatMap(this::persistCatalog);
    }

    private Mono<Void> persistCatalog(TercXmlDocument document) {
        List<TerritorialUnitImportRecord> records = document.catalog().rows().stream()
                                                            .map(tercRecordMapper::map)
                                                            .flatMap(Optional::stream)
                                                            .toList();

        validateRecords(records);

        Map<String, Long> unitsByLevel = records.stream()
                                                .collect(Collectors.groupingBy(
                                                        TerritorialUnitImportRecord::unitLevel,
                                                        Collectors.counting()
                                                ));

        log.info(
                "Starting TERC import: sourceRecords={}, mappedRecords={}, skippedRecords={}, unitsByLevel={}",
                document.catalog().rows().size(),
                records.size(),
                document.catalog().rows().size() - records.size(),
                unitsByLevel
        );

        return Flux.fromIterable(records)
                   .concatMap(this::upsert)
                   .reduce(0, Integer::sum)
                   .flatMap(affectedRows ->
                           territorialUnitRepository.updateParentReferences()
                                                    .doOnNext(updatedRelations -> log.info(
                                                            "TERC import completed: processedRecords={}, affectedRows={}, updatedParentRelations={}",
                                                            records.size(),
                                                            affectedRows,
                                                            updatedRelations
                                                    ))
                   )
                   .then();
    }

    private Mono<Integer> upsert(TerritorialUnitImportRecord record) {
        return territorialUnitRepository.upsert(
                record.terytCode(),
                record.name(),
                record.additionalName(),
                record.unitLevel(),
                record.rodz(),
                record.sourceStateAt()
        );
    }

    private void validateRecords(List<TerritorialUnitImportRecord> records) {
        Set<String> terytCodes = records.stream()
                                        .map(TerritorialUnitImportRecord::terytCode)
                                        .collect(Collectors.toSet());

        if (terytCodes.size() != records.size()) {
            throw new IllegalStateException("Duplicate TERYT codes found in catalog");
        }

        List<TerritorialUnitImportRecord> missingParents = records.stream()
                                                                  .filter(record -> record.parentTerytCode() != null)
                                                                  .filter(record -> !terytCodes.contains(record.parentTerytCode()))
                                                                  .toList();

        if (!missingParents.isEmpty()) {
            throw new IllegalStateException(
                    "TERC catalog contains units with missing parents: " + missingParents.size()
            );
        }
    }
}