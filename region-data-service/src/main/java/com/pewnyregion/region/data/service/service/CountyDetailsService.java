package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.entity.TerritorialUnitEntity;
import com.pewnyregion.region.data.service.entity.VariableEntity;
import com.pewnyregion.region.data.service.exception.NotFoundException;
import com.pewnyregion.region.data.service.model.CountyDetailsResponse;
import com.pewnyregion.region.data.service.model.VariableDetail;
import com.pewnyregion.region.data.service.model.YearlyData;
import com.pewnyregion.region.data.service.repository.TerritorialUnitRepository;
import com.pewnyregion.region.data.service.repository.VariableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CountyDetailsService {

    private static final String COUNTY_LEVEL = "COUNTY";

    private final TerritorialUnitRepository territorialUnitRepository;
    private final VariableRepository variableRepository;
    private final CountyDetailsCacheService countyDetailsCacheService;

    public Mono<CountyDetailsResponse> getCountyDetails(String terytCode, List<String> apiNames) {
        List<String> distinctApiNames = apiNames.stream()
                                                .distinct()
                                                .toList();

        return Mono.zip(findCountyOrThrow(terytCode), findVariablesOrThrow(distinctApiNames))
                   .flatMap(result -> loadCountyDetails(terytCode, result.getT1(), result.getT2()));
    }

    private Mono<TerritorialUnitEntity> findCountyOrThrow(String terytCode) {
        return territorialUnitRepository
                .findByTerytCodeAndUnitLevelAndActive(terytCode, COUNTY_LEVEL, true)
                .switchIfEmpty(Mono.error(new NotFoundException("County with TERYT code " + terytCode + " does not exist")));
    }

    private Mono<List<VariableEntity>> findVariablesOrThrow(List<String> apiNames) {
        return variableRepository.findByApiNameIn(apiNames)
                                 .collectMap(VariableEntity::getApiName)
                                 .filter(byName -> byName.keySet().containsAll(apiNames))
                                 .switchIfEmpty(Mono.error(new NotFoundException("One or more variable apiNames do not exist")))
                                 .map(byName -> apiNames.stream()
                                                        .map(byName::get)
                                                        .toList()
                                 );
    }

    private Mono<CountyDetailsResponse> loadCountyDetails(String terytCode, TerritorialUnitEntity county, List<VariableEntity> variables) {
        List<Integer> variableIds = variables.stream()
                                             .map(VariableEntity::getId)
                                             .toList();

        return countyDetailsCacheService.getYearlyData(county.getId(), variableIds)
                                        .map(details -> buildResponse(terytCode, variables, details));
    }

    private CountyDetailsResponse buildResponse(String terytCode, List<VariableEntity> variables, Map<Integer, List<YearlyData>> detailsByVariableId) {
        List<VariableDetail> variableDetails = variables.stream()
                                                        .map(variable -> new VariableDetail(variable.getApiName(),
                                                                                            detailsByVariableId.getOrDefault(variable.getId(), List.of())))
                                                        .toList();

        return new CountyDetailsResponse(terytCode, variableDetails);
    }
}