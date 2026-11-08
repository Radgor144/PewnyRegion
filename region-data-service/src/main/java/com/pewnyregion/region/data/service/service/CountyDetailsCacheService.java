package com.pewnyregion.region.data.service.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.pewnyregion.region.data.service.model.TerritorialUnitScoreWithAverage;
import com.pewnyregion.region.data.service.model.TerritorialUnitVariableCacheKey;
import com.pewnyregion.region.data.service.model.YearlyData;
import com.pewnyregion.region.data.service.repository.TerritorialUnitVariableScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountyDetailsCacheService {

    private final Cache<TerritorialUnitVariableCacheKey, List<YearlyData>> countyDetailsCache;

    private final TerritorialUnitVariableScoreRepository scoreRepository;

    public Mono<Map<Integer, List<YearlyData>>> getYearlyData(
            Long territorialUnitId,
            List<Integer> variableIds
    ) {
        return Mono.defer(() -> {
            if (variableIds.isEmpty()) {
                return Mono.just(Map.of());
            }

            Map<Integer, List<YearlyData>> result = new HashMap<>();
            List<Integer> missingVariableIds = new ArrayList<>();

            for (Integer variableId : new LinkedHashSet<>(variableIds)) {
                TerritorialUnitVariableCacheKey key =
                        new TerritorialUnitVariableCacheKey(
                                territorialUnitId,
                                variableId
                        );

                List<YearlyData> cached = countyDetailsCache.getIfPresent(key);

                if (cached == null) {
                    missingVariableIds.add(variableId);
                } else {
                    result.put(variableId, cached);
                }
            }

            log.debug(
                    "Territorial unit details cache: unitId={}, hits={}, misses={}",
                    territorialUnitId,
                    result.size(),
                    missingVariableIds.size()
            );

            if (missingVariableIds.isEmpty()) {
                return Mono.just(result);
            }

            return loadMissingDetails(
                    territorialUnitId,
                    missingVariableIds
            ).map(loaded -> {
                result.putAll(loaded);
                return result;
            });
        });
    }

    private Mono<Map<Integer, List<YearlyData>>> loadMissingDetails(
            Long territorialUnitId,
            List<Integer> missingVariableIds
    ) {
        return scoreRepository.findUnitDetails(
                                      territorialUnitId,
                                      missingVariableIds
                              )
                              .collectList()
                              .map(scores -> mapScoresToYearlyData(
                                      scores,
                                      missingVariableIds
                              ))
                              .doOnNext(details -> cacheDetails(
                                      territorialUnitId,
                                      details
                              ));
    }

    private Map<Integer, List<YearlyData>> mapScoresToYearlyData(
            List<TerritorialUnitScoreWithAverage> scores,
            List<Integer> variableIds
    ) {
        Map<Integer, List<YearlyData>> detailsByVariableId = scores.stream()
                                                                   .collect(Collectors.groupingBy(
                                                                           TerritorialUnitScoreWithAverage::variableId,
                                                                           HashMap::new,
                                                                           Collectors.mapping(
                                                                                   score -> new YearlyData(
                                                                                           score.year(),
                                                                                           score.rawValue(),
                                                                                           score.averageScore()
                                                                                   ),
                                                                                   Collectors.toList()
                                                                           )
                                                                   ));

        variableIds.forEach(variableId ->
                detailsByVariableId.putIfAbsent(variableId, List.of())
        );

        return detailsByVariableId;
    }

    private void cacheDetails(
            Long territorialUnitId,
            Map<Integer, List<YearlyData>> detailsByVariableId
    ) {
        detailsByVariableId.forEach((variableId, details) ->
                countyDetailsCache.put(
                        new TerritorialUnitVariableCacheKey(
                                territorialUnitId,
                                variableId
                        ),
                        details
                )
        );
    }

    public void invalidateAll() {
        countyDetailsCache.invalidateAll();
        log.info("Territorial unit details cache invalidated");
    }
}