package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.client.BdlApiClient;
import com.pewnyregion.region.data.service.component.BdlUnitTerytMapper;
import com.pewnyregion.region.data.service.entity.TerritorialUnitEntity;
import com.pewnyregion.region.data.service.model.BdlResponse;
import com.pewnyregion.region.data.service.model.BdlUnit;
import com.pewnyregion.region.data.service.model.MappingResult;
import com.pewnyregion.region.data.service.repository.TerritorialUnitExternalIdRepository;
import com.pewnyregion.region.data.service.repository.TerritorialUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BdlUnitMappingImportService {

    private static final int PAGE_SIZE = 100;
    private static final List<Integer> LEVELS = List.of(5); // List.of(2, 5, 6);
    private static final String COUNTY_LEVEL = "COUNTY";
    private static final String BDL_PROVIDER = "BDL";

    private final BdlApiClient bdlApiClient;
    private final BdlUnitTerytMapper bdlUnitTerytMapper;
    private final TerritorialUnitRepository territorialUnitRepository;
    private final TerritorialUnitExternalIdRepository territorialUnitExternalIdRepository;

    public Mono<Void> importMappings() {
        return fetchMappings()
                .flatMap(results -> {
                    List<MappingResult> matchedMappings = results.stream()
                                                                 .filter(result -> result.territorialUnitId() != null)
                                                                 .toList();

                    long distinctTerytCodes = matchedMappings.stream()
                                                             .map(MappingResult::terytCode)
                                                             .distinct()
                                                             .count();

                    long distinctExternalIds = matchedMappings.stream()
                                                              .map(result -> result.unit().id())
                                                              .distinct()
                                                              .count();

                    if (distinctTerytCodes != matchedMappings.size()
                            || distinctExternalIds != matchedMappings.size()) {
                        return Mono.error(new IllegalStateException(
                                "Duplicate BDL unit mappings detected"
                        ));
                    }

                    log.info(
                            "Starting BDL mapping import: totalUnits={}, matchedMappings={}, skippedMappings={}",
                            results.size(),
                            matchedMappings.size(),
                            results.size() - matchedMappings.size()
                    );

                    return Flux.fromIterable(matchedMappings)
                               .concatMap(this::upsertMapping)
                               .reduce(0, Integer::sum)
                               .doOnNext(affectedRows -> log.info(
                                       "BDL mapping import completed: importedMappings={}, affectedRows={}",
                                       matchedMappings.size(),
                                       affectedRows
                               ));
                })
                .then();
    }

    public Mono<Void> ensureMappings() {
        return Mono.zip(territorialUnitRepository.countByUnitLevelAndActive(COUNTY_LEVEL, true),
                           territorialUnitExternalIdRepository.countByProviderAndUnitLevel(BDL_PROVIDER, COUNTY_LEVEL))
                   .flatMap(counts -> {
                       long expectedMappings = counts.getT1();
                       long existingMappings = counts.getT2();

                       if (expectedMappings == 0) {
                           return Mono.error(new IllegalStateException(
                                   "No active counties found in TERYT catalog"
                           ));
                       }

                       if (expectedMappings == existingMappings) {
                           log.info(
                                   "BDL mappings are complete: {}/{}",
                                   existingMappings,
                                   expectedMappings
                           );

                           return Mono.empty();
                       }

                       log.info(
                               "BDL mappings are incomplete: {}/{}. Starting mapping import",
                               existingMappings,
                               expectedMappings
                       );

                       return importMappings();
                   });
    }

    private Mono<Integer> upsertMapping(MappingResult result) {
        return territorialUnitExternalIdRepository.upsert(
                result.territorialUnitId(),
                BDL_PROVIDER,
                result.unit().id(),
                String.valueOf(result.unit().level())
        );
    }

    private Mono<List<MappingResult>> fetchMappings() {
        return territorialUnitRepository.findAll()
                                        .collectMap(
                                                TerritorialUnitEntity::getTerytCode,
                                                TerritorialUnitEntity::getId
                                        )
                                        .flatMap(territorialUnitIds ->
                                                Flux.fromIterable(LEVELS)
                                                    .concatMap(this::fetchUnits)
                                                    .map(unit -> mapUnit(unit, territorialUnitIds))
                                                    .collectList()
                                        );
    }

    private Flux<BdlUnit> fetchUnits(int level) {
        return fetchPage(0, level)
                .expand(response -> {
                    if (response.links() == null || response.links().next() == null) {
                        return Mono.empty();
                    }

                    return fetchPage(response.page() + 1, level);
                })
                .flatMapIterable(BdlResponse::results);
    }

    private Mono<BdlResponse> fetchPage(int page, int level) {
        return Mono.delay(Duration.ofMillis(500))
                   .then(bdlApiClient.fetchUnits(page, level, PAGE_SIZE));
    }

    private MappingResult mapUnit(BdlUnit unit, Map<String, Long> territorialUnitIds) {
        String terytCode = bdlUnitTerytMapper.map(unit)
                                             .orElseThrow(() -> new IllegalStateException(
                                                     "Cannot map BDL unit: " + unit.id()
                                             ));

        return new MappingResult(unit, terytCode, territorialUnitIds.get(terytCode));
    }
}
