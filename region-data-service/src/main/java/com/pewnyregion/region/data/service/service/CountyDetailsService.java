package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.entity.CountyEntity;
import com.pewnyregion.region.data.service.entity.CountyVariableScoreEntity;
import com.pewnyregion.region.data.service.exception.NotFoundException;
import com.pewnyregion.region.data.service.model.AverageScoreDto;
import com.pewnyregion.region.data.service.model.CountyDetailsResponse;
import com.pewnyregion.region.data.service.model.VariableDetail;
import com.pewnyregion.region.data.service.model.YearlyData;
import com.pewnyregion.region.data.service.repository.BdlVariableIdRepository;
import com.pewnyregion.region.data.service.repository.CountyRepository;
import com.pewnyregion.region.data.service.repository.CountyVariableScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountyDetailsService {

    private final CountyRepository countyRepository;
    private final BdlVariableIdRepository bdlVariableIdRepository;
    private final CountyVariableScoreRepository countyVariableScoreRepository;

    public Mono<CountyDetailsResponse> getCountyDetails(String terytCode, List<Integer> bdlIds) {
        return findCountyOrThrow(terytCode)
                .flatMap(county -> validateBdlIds(bdlIds).thenReturn(county))
                .flatMap(county -> fetchCountyDetails(county, bdlIds));
    }

    private Mono<CountyEntity> findCountyOrThrow(String terytCode) {
        return countyRepository.findByTerytCode(terytCode)
                               .switchIfEmpty(Mono.error(() -> new NotFoundException("County with terytCode " + terytCode + " does not exist")));
    }

    private Mono<Void> validateBdlIds(List<Integer> bdlIds) {
        long distinctRequested = bdlIds.stream().distinct().count();

        return bdlVariableIdRepository.findByBdlIdIn(bdlIds)
                                      .count()
                                      .filter(foundCount -> foundCount == distinctRequested)
                                      .switchIfEmpty(Mono.error(() -> new NotFoundException("One or more BDL variable IDs do not exist")))
                                      .then();
    }

    private Mono<CountyDetailsResponse> fetchCountyDetails(CountyEntity county, List<Integer> bdlIds) {
        Mono<List<CountyVariableScoreEntity>> countyScores = countyVariableScoreRepository
                .findByCountyIdAndBdlIds(county.getId(), bdlIds)
                .collectList();

        Mono<List<AverageScoreDto>> averageScores = countyVariableScoreRepository.findAverageScoresByBdlIds(bdlIds)
                                                                                 .collectList();

        return Mono.zip(countyScores, averageScores, (scores, averages) -> buildResponse(county.getTerytCode(), scores, averages));
    }

    private CountyDetailsResponse buildResponse(String terytCode,
                                                List<CountyVariableScoreEntity> countyScores,
                                                List<AverageScoreDto> averageScores) {

        Map<Integer, Map<Integer, Double>> averageScoresMap = buildAverageScoresMap(averageScores);
        List<VariableDetail> variables = buildVariableDetails(countyScores, averageScoresMap);

        return new CountyDetailsResponse(terytCode, variables);
    }

    private Map<Integer, Map<Integer, Double>> buildAverageScoresMap(List<AverageScoreDto> averageScores) {
        return averageScores.stream()
                            .collect(Collectors.groupingBy(
                                    AverageScoreDto::bdlVariableId,
                                    Collectors.toMap(AverageScoreDto::year,
                                                     AverageScoreDto::averageScore)));
    }

    private List<VariableDetail> buildVariableDetails(List<CountyVariableScoreEntity> countyScores,
                                                      Map<Integer, Map<Integer, Double>> averageScoresMap) {

        return countyScores.stream()
                           .collect(Collectors.groupingBy(CountyVariableScoreEntity::getBdlVariableId))
                           .entrySet()
                           .stream()
                           .map(entry -> createVariableDetail(entry.getKey(),
                                                              entry.getValue(),
                                                              averageScoresMap))
                           .toList();
    }

    private VariableDetail createVariableDetail(Integer variableId,
                                                List<CountyVariableScoreEntity> countyScores,
                                                Map<Integer, Map<Integer, Double>> averageScoresMap) {

        Map<Integer, Double> averages = averageScoresMap.getOrDefault(variableId, Map.of());

        List<YearlyData> yearlyData = countyScores.stream()
                                                  .map(entity -> new YearlyData(entity.getYear(),
                                                                                entity.getRawValue(),
                                                                                averages.get(entity.getYear())))
                                                  .toList();

        return new VariableDetail(variableId, yearlyData);
    }
}
