package com.pewnyregion.region.data.service.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.pewnyregion.region.data.service.model.CountyScoreWithAverage;
import com.pewnyregion.region.data.service.model.CountyVariableCacheKey;
import com.pewnyregion.region.data.service.model.YearlyData;
import com.pewnyregion.region.data.service.repository.CountyVariableScoreRepository;
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

    private final Cache<CountyVariableCacheKey, List<YearlyData>> countyDetailsCache;
    private final CountyVariableScoreRepository countyVariableScoreRepository;

    public Mono<Map<Integer, List<YearlyData>>> getYearlyData(String countyId, List<Integer> variableIds) {
        return Mono.defer(() -> {
            Map<Integer, List<YearlyData>> result = new HashMap<>();
            List<Integer> missing = new ArrayList<>();

            for (Integer variableId : new LinkedHashSet<>(variableIds)) {
                List<YearlyData> cached = countyDetailsCache.getIfPresent(new CountyVariableCacheKey(countyId, variableId));
                if (cached != null) {
                    result.put(variableId, cached);
                } else {
                    missing.add(variableId);
                }
            }

            log.debug("County details cache: countyId={}, hits={}, misses={}",
                    countyId, result.keySet(), missing);

            if (missing.isEmpty()) {
                return Mono.just(result);
            }

            return loadMissingDetails(countyId, missing)
                    .map(loaded -> {
                        result.putAll(loaded);
                        return result;
                    });
        });
    }

    private Mono<Map<Integer, List<YearlyData>>> loadMissingDetails(String countyId, List<Integer> missingIds) {
        return countyVariableScoreRepository
                .findCountyDetailsByCountyIdAndVariableIds(countyId, missingIds)
                .collectList()
                .map(scores -> mapScoresToYearlyData(scores, missingIds))
                .doOnNext(details -> cacheDetails(countyId, details));
    }

    private Map<Integer, List<YearlyData>> mapScoresToYearlyData(List<CountyScoreWithAverage> scores, List<Integer> variableIds) {
        Map<Integer, List<YearlyData>> detailsByVariableId =
                scores.stream()
                      .collect(Collectors.groupingBy(
                              CountyScoreWithAverage::bdlVariableId,
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

        variableIds.forEach(variableId -> detailsByVariableId.putIfAbsent(variableId, List.of()));
        return detailsByVariableId;
    }

    private void cacheDetails(String countyId, Map<Integer, List<YearlyData>> detailsByVariableId) {
        detailsByVariableId.forEach((variableId, details) ->
                countyDetailsCache.put(
                        new CountyVariableCacheKey(countyId, variableId),
                        details
                )
        );
    }

    public void invalidateAll() {
        countyDetailsCache.invalidateAll();
        log.info("County details cache invalidated");
    }
}