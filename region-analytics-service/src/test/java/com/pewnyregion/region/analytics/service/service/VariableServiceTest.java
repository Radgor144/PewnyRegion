package com.pewnyregion.region.analytics.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pewnyregion.region.analytics.service.entity.BdlVariableEntity;
import com.pewnyregion.region.analytics.service.entity.BdlVariableIdEntity;
import com.pewnyregion.region.analytics.service.model.VariableResponse;
import com.pewnyregion.region.analytics.service.repository.BdlVariableIdRepository;
import com.pewnyregion.region.analytics.service.repository.BdlVariableRepository;
import com.pewnyregion.region.analytics.service.utils.JsonFileReader;
import com.pewnyregion.region.analytics.service.utils.VariableTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.pewnyregion.region.analytics.service.utils.TestConstants.GET_ALL_VARIABLES_RESPONSE_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VariableServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private VariableService variableService;
    private List<BdlVariableEntity> bdlVariables;
    private List<BdlVariableIdEntity> bdlVariablesId;

    @Mock
    private BdlVariableRepository variableRepository;
    @Mock
    private BdlVariableIdRepository variableIdRepository;

    @BeforeEach
    void setUp() {
        variableService = new VariableService(variableRepository, variableIdRepository);
        bdlVariables = VariableTestFixture.createBdlVariables();
        bdlVariablesId = VariableTestFixture.createBdlVariablesIds();
    }

    @Test
    public void getAllVariables_ShouldReturnMappedVariables() throws IOException {
        List<VariableResponse> expectedList = List.of(
                JsonFileReader.readJson(objectMapper, GET_ALL_VARIABLES_RESPONSE_JSON, VariableResponse[].class)
        );

        when(variableRepository.findAll()).thenReturn(Flux.fromIterable(bdlVariables));
        when(variableIdRepository.findByBdlVariableIdIn(any())).thenReturn(Flux.fromIterable(bdlVariablesId));

        StepVerifier.create(variableService.getAllVariables())
                    .recordWith(ArrayList::new)
                    .expectNextCount(bdlVariables.size())
                    .consumeRecordedWith(actualList ->
                            assertThat(actualList).containsExactlyInAnyOrderElementsOf(expectedList))
                    .verifyComplete();
    }

    @Test
    public void getAllVariableIds_ShouldReturnAllIds_WhenDataExists() {
        when(variableIdRepository.findAll()).thenReturn(Flux.fromIterable(bdlVariablesId));

        List<Integer> expectedIds = bdlVariablesId.stream()
                                                  .map(BdlVariableIdEntity::getBdlId)
                                                  .toList();

        StepVerifier.create(variableService.getAllVariableIds())
                    .assertNext(actualList ->
                            assertThat(actualList).containsExactlyInAnyOrderElementsOf(expectedIds))
                    .verifyComplete();
    }

    @Test
    public void getVariableIdsByApiNames_ShouldReturnMatchingIds_WhenApiNamesAreValid() {
        List<String> apiNames = List.of("crimes", "population_in_thousands", "population_density", "unemployment", "gross_salary");
        List<Integer> expectedIds = bdlVariablesId.stream()
                                                  .map(BdlVariableIdEntity::getBdlId)
                                                  .toList();

        when(variableRepository.findByApiNameIn(apiNames)).thenReturn(Flux.fromIterable(bdlVariables));
        when(variableIdRepository.findByBdlVariableIdIn(any())).thenReturn(Flux.fromIterable(bdlVariablesId));

        StepVerifier.create(variableService.getVariableIdsByApiNames(apiNames))
                    .assertNext(actualList ->
                            assertThat(actualList).containsExactlyInAnyOrderElementsOf(expectedIds))
                    .verifyComplete();
    }

    @Test
    public void getAllVariables_ShouldReturnEmptyFlux_WhenNoVariablesExist() {
        when(variableRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(variableService.getAllVariables())
                    .verifyComplete();

        verifyNoInteractions(variableIdRepository);
    }

    @Test
    public void getAllVariables_ShouldReturnVariablesWithEmptyIdsList_WhenNoIdsInDatabase() {
        when(variableRepository.findAll()).thenReturn(Flux.fromIterable(bdlVariables));
        when(variableIdRepository.findByBdlVariableIdIn(any())).thenReturn(Flux.empty());

        StepVerifier.create(variableService.getAllVariables())
                    .recordWith(ArrayList::new)
                    .expectNextCount(bdlVariables.size())
                    .consumeRecordedWith(actualList -> {
                        assertThat(actualList).allMatch(response -> response.bdlIds().isEmpty());
                    })
                    .verifyComplete();
    }

    @Test
    public void getVariableIdsByApiNames_ShouldReturnEmpty_WhenApiNamesNotFound() {
        List<String> nonExistentNames = List.of("fake_variable_1", "fake_variable_2");
        when(variableRepository.findByApiNameIn(nonExistentNames)).thenReturn(Flux.empty());

        StepVerifier.create(variableService.getVariableIdsByApiNames(nonExistentNames))
                    .assertNext(actualList -> assertThat(actualList).isEmpty())
                    .verifyComplete();

        verifyNoInteractions(variableIdRepository);
    }
}
