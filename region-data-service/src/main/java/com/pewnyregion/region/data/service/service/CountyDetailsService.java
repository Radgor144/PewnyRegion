
package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.entity.CountyEntity;
import com.pewnyregion.region.data.service.entity.VariableEntity;
import com.pewnyregion.region.data.service.exception.NotFoundException;
import com.pewnyregion.region.data.service.model.CountyDetailsResponse;
import com.pewnyregion.region.data.service.model.VariableDetail;
import com.pewnyregion.region.data.service.model.YearlyData;
import com.pewnyregion.region.data.service.repository.CountyRepository;
import com.pewnyregion.region.data.service.repository.VariableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CountyDetailsService {

    private final CountyRepository countyRepository;
    private final VariableRepository variableRepository;
    private final CountyDetailsCacheService countyDetailsCacheService;

    public Mono<CountyDetailsResponse> getCountyDetails(String terytCode, List<String> apiNames) {
        List<String> distinctApiNames = apiNames.stream().distinct().toList();

        return Mono.zip(
                           findCountyOrThrow(terytCode),
                           findVariablesOrThrow(distinctApiNames)
                   )
                   .flatMap(tuple ->
                           loadCountyDetails(terytCode, tuple.getT1(), tuple.getT2())
                   );
    }

    private Mono<CountyEntity> findCountyOrThrow(String terytCode) {
        return countyRepository.findByTerytCode(terytCode)
                               .switchIfEmpty(Mono.error(
                                       new NotFoundException(
                                               "County with terytCode " + terytCode + " does not exist"
                                       )
                               ));
    }

    private Mono<List<VariableEntity>> findVariablesOrThrow(List<String> apiNames) {
        return variableRepository.findByApiNameIn(apiNames)
                                 .collectMap(VariableEntity::getApiName)
                                 .filter(byName -> byName.keySet().containsAll(apiNames))
                                 .switchIfEmpty(Mono.error(
                                         new NotFoundException(
                                                 "One or more variable apiNames do not exist"
                                         )
                                 ))
                                 .map(byName ->
                                         apiNames.stream()
                                                 .map(byName::get)
                                                 .toList()
                                 );
    }

    private Mono<CountyDetailsResponse> loadCountyDetails(
            String terytCode,
            CountyEntity county,
            List<VariableEntity> variables
    ) {
        List<Integer> variableIds = variables.stream()
                                             .map(VariableEntity::getId)
                                             .toList();

        return countyDetailsCacheService.getYearlyData(county.getId(), variableIds)
                                        .map(detailsByVariableId ->
                                                buildResponse(terytCode, variables, detailsByVariableId)
                                        );
    }

    private CountyDetailsResponse buildResponse(
            String terytCode,
            List<VariableEntity> variables,
            Map<Integer, List<YearlyData>> detailsByVariableId
    ) {
        List<VariableDetail> variableDetails = variables.stream()
                                                        .map(variable ->
                                                                new VariableDetail(
                                                                        variable.getApiName(),
                                                                        detailsByVariableId.getOrDefault(
                                                                                variable.getId(),
                                                                                List.of()
                                                                        )
                                                                )
                                                        )
                                                        .toList();

        return new CountyDetailsResponse(terytCode, variableDetails);
    }
}
