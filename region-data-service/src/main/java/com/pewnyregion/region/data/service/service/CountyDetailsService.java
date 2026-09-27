package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.entity.CountyEntity;
import com.pewnyregion.region.data.service.exception.NotFoundException;
import com.pewnyregion.region.data.service.model.CountyDetailsResponse;
import com.pewnyregion.region.data.service.model.CountyScoreWithAverage;
import com.pewnyregion.region.data.service.model.VariableDetail;
import com.pewnyregion.region.data.service.model.YearlyData;
import com.pewnyregion.region.data.service.repository.BdlVariableIdRepository;
import com.pewnyregion.region.data.service.repository.CountyRepository;
import com.pewnyregion.region.data.service.repository.CountyVariableScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
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
                .flatMap(county -> countyVariableScoreRepository.findCountyDetailsByCountyIdAndBdlIds(county.getId(), bdlIds)
                                                                .collectList()
                                                                .map(scores -> buildResponse(county.getTerytCode(), scores)));
    }

    private Mono<CountyEntity> findCountyOrThrow(String terytCode) {
        return countyRepository.findByTerytCode(terytCode)
                               .switchIfEmpty(Mono.error(() -> new NotFoundException("County with terytCode " + terytCode + " does not exist")));
    }

    private Mono<Void> validateBdlIds(List<Integer> bdlIds) {
        long distinctRequested = bdlIds.stream()
                                       .distinct()
                                       .count();

        return bdlVariableIdRepository.findByBdlIdIn(bdlIds)
                                      .count()
                                      .filter(foundCount -> foundCount == distinctRequested)
                                      .switchIfEmpty(Mono.error(() -> new NotFoundException("One or more BDL variable IDs do not exist")))
                                      .then();
    }

    private CountyDetailsResponse buildResponse(String terytCode, List<CountyScoreWithAverage> scores) {
        List<VariableDetail> variables = scores.stream()
                                               .collect(Collectors.groupingBy(CountyScoreWithAverage::bdlVariableId))
                                               .entrySet()
                                               .stream()
                                               .map(entry -> new VariableDetail(
                                                       entry.getKey(),
                                                       entry.getValue().stream()
                                                            .map(score -> new YearlyData(
                                                                    score.year(),
                                                                    score.rawValue(),
                                                                    score.averageScore()
                                                            ))
                                                            .toList()
                                               ))
                                               .toList();

        return new CountyDetailsResponse(terytCode, variables);
    }
}
