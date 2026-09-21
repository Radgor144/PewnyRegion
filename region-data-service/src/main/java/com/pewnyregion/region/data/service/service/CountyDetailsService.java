package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.entity.CountyVariableScoreEntity;
import com.pewnyregion.region.data.service.model.CountyDetailsResponse;
import com.pewnyregion.region.data.service.model.VariableDetail;
import com.pewnyregion.region.data.service.model.YearlyData;
import com.pewnyregion.region.data.service.repository.CountyDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountyDetailsService {

    private final CountyDetailsRepository countyDetailsRepository;

    public Mono<CountyDetailsResponse> getCountyDetails(String terytCode, List<Integer> bdlVariableIds) {
        return countyDetailsRepository.findByTerytCodeAndBdlIds(terytCode, bdlVariableIds)
                                      .groupBy(CountyVariableScoreEntity::getBdlVariableId)
                                      .flatMap(variableGroup -> variableGroup
                                              .map(this::mapToYearlyData)
                                              .collectList()
                                              .map(yearlyValues -> new VariableDetail(variableGroup.key(), yearlyValues))
                                      )
                                      .collectList()
                                      .map(variables -> buildCountyDetailsResponse(terytCode, variables));
    }

    private YearlyData mapToYearlyData(CountyVariableScoreEntity entity) {
        return new YearlyData(entity.getYear(), entity.getRawValue(), entity.getNormalizedScore());
    }

    private CountyDetailsResponse buildCountyDetailsResponse(String terytCode, List<VariableDetail> variables) {
        return new CountyDetailsResponse(terytCode, variables);
    }
}