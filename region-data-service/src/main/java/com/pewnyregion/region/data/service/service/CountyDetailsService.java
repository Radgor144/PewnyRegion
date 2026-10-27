package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.entity.BdlVariableEntity;
import com.pewnyregion.region.data.service.entity.CountyEntity;
import com.pewnyregion.region.data.service.exception.NotFoundException;
import com.pewnyregion.region.data.service.model.CountyDetailsResponse;
import com.pewnyregion.region.data.service.model.VariableDetail;
import com.pewnyregion.region.data.service.model.YearlyData;
import com.pewnyregion.region.data.service.repository.BdlVariableRepository;
import com.pewnyregion.region.data.service.repository.CountyRepository;
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
    private final BdlVariableRepository bdlVariableRepository;
    private final CountyDetailsCacheService countyDetailsCacheService;

    public Mono<CountyDetailsResponse> getCountyDetails(String terytCode, List<String> apiNames) {
        List<String> distinctApiNames = apiNames.stream().distinct().toList();

        return Mono.zip(findCountyOrThrow(terytCode), findVariablesOrThrow(distinctApiNames))
                   .flatMap(t -> loadCountyDetails(terytCode, t.getT1(), t.getT2()));
    }

    private Mono<CountyEntity> findCountyOrThrow(String terytCode) {
        return countyRepository.findByTerytCode(terytCode)
                               .switchIfEmpty(Mono.error(() -> new NotFoundException("County with terytCode " + terytCode + " does not exist")));
    }

    private Mono<List<BdlVariableEntity>> findVariablesOrThrow(List<String> apiNames) {
        return bdlVariableRepository.findByApiNameIn(apiNames)
                                    .collectMap(BdlVariableEntity::getApiName)
                                    .filter(byName -> byName.keySet().containsAll(apiNames))
                                    .switchIfEmpty(Mono.error(() -> new NotFoundException("One or more variable apiNames do not exist")))
                                    .map(byName -> apiNames.stream().map(byName::get).toList());
    }

    private Mono<CountyDetailsResponse> loadCountyDetails(String terytCode,
                                                          CountyEntity county,
                                                          List<BdlVariableEntity> variables) {
        List<Integer> variableIds = variables.stream()
                                             .map(BdlVariableEntity::getId)
                                             .toList();

        return countyDetailsCacheService.getYearlyData(county.getId(), variableIds)
                                        .map(detailsByVariableId -> buildResponse(terytCode,
                                                                                  variables,
                                                                                  detailsByVariableId));
    }

    private CountyDetailsResponse buildResponse(String terytCode,
                                                List<BdlVariableEntity> variables,
                                                Map<Integer, List<YearlyData>> detailsByVariableId) {
        List<VariableDetail> variableDetails = variables.stream()
                                                        .map(variable -> new VariableDetail(variable.getApiName(),
                                                                                            detailsByVariableId.getOrDefault(variable.getId(),
                                                                                                                             List.of())))
                                                        .toList();

        return new CountyDetailsResponse(terytCode, variableDetails);
    }
}
