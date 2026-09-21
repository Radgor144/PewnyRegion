package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.entity.CountyVariableScoreEntity;
import com.pewnyregion.region.data.service.model.AverageScoreDto;
import com.pewnyregion.region.data.service.model.CountyDetailsResponse;
import com.pewnyregion.region.data.service.model.VariableDetail;
import com.pewnyregion.region.data.service.model.YearlyData;
import com.pewnyregion.region.data.service.repository.CountyDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountyDetailsService {

    private final CountyDetailsRepository countyDetailsRepository;

    public Mono<CountyDetailsResponse> getCountyDetails(String terytCode, List<Integer> bdlVariableIds) {
        Mono<List<CountyVariableScoreEntity>> countyScoresMono =
                countyDetailsRepository.findByTerytCodeAndBdlIds(terytCode, bdlVariableIds).collectList();

        Mono<List<AverageScoreDto>> averageScoresMono =
                countyDetailsRepository.findAverageScoresByBdlIds(bdlVariableIds).collectList();

        return Mono.zip(countyScoresMono, averageScoresMono)
                   .map(tuple -> buildCombinedResponse(terytCode, tuple.getT1(), tuple.getT2()));
    }

    private CountyDetailsResponse buildCombinedResponse(String terytCode,
                                                        List<CountyVariableScoreEntity> countyScores,
                                                        List<AverageScoreDto> avgScores) {

        Map<Integer, Map<Integer, Double>> avgScoresMap = groupAvgScoresByVariableAndYear(avgScores);
        Map<Integer, List<CountyVariableScoreEntity>> groupedCountyScores = groupCountyScoresByVariable(countyScores);

        List<VariableDetail> variables = groupedCountyScores.entrySet().stream()
                                                            .map(entry -> createVariableDetail(entry, avgScoresMap))
                                                            .toList();

        return new CountyDetailsResponse(terytCode, variables);
    }

    private Map<Integer, Map<Integer, Double>> groupAvgScoresByVariableAndYear(List<AverageScoreDto> avgScores) {
        return avgScores.stream()
                        .collect(Collectors.groupingBy(
                                AverageScoreDto::bdlVariableId,
                                Collectors.toMap(
                                        AverageScoreDto::year,
                                        AverageScoreDto::averageScore,
                                        (existing, replacement) -> existing
                                )
                        ));
    }

    private Map<Integer, List<CountyVariableScoreEntity>> groupCountyScoresByVariable(List<CountyVariableScoreEntity> countyScores) {
        return countyScores.stream()
                           .collect(Collectors.groupingBy(CountyVariableScoreEntity::getBdlVariableId));
    }

    private VariableDetail createVariableDetail(Map.Entry<Integer, List<CountyVariableScoreEntity>> entry,
                                                Map<Integer, Map<Integer, Double>> avgScoresMap) {
        Integer variableId = entry.getKey();
        Map<Integer, Double> averagesForVariable = avgScoresMap.getOrDefault(variableId, Collections.emptyMap());
        List<YearlyData> yearlyValues = mapToYearlyDataList(entry.getValue(), averagesForVariable);

        return new VariableDetail(variableId, yearlyValues);
    }

    private List<YearlyData> mapToYearlyDataList(List<CountyVariableScoreEntity> entities,
                                                 Map<Integer, Double> averagesForVariable) {
        return entities.stream()
                       .map(entity -> new YearlyData(
                               entity.getYear(),
                               entity.getRawValue(),
                               averagesForVariable.get(entity.getYear())
                       ))
                       .toList();
    }
}
