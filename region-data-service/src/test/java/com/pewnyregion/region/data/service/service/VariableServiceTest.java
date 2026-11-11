package com.pewnyregion.region.data.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pewnyregion.region.data.service.entity.VariableEntity;
import com.pewnyregion.region.data.service.model.VariableResponse;
import com.pewnyregion.region.data.service.repository.VariableRepository;
import com.pewnyregion.region.data.service.utils.JsonFileReader;
import com.pewnyregion.region.data.service.utils.VariableTestFixture;
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

import static com.pewnyregion.region.data.service.utils.TestConstants.GET_ALL_VARIABLES_RESPONSE_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VariableServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private VariableService variableService;
    private List<VariableEntity> variables;

    @Mock
    private VariableRepository variableRepository;

    @BeforeEach
    void setUp() {
        variableService = new VariableService(variableRepository);
        variables = VariableTestFixture.createVariables();
    }

    @Test
    void getAllVariables_returnsMappedVariables() throws IOException {
        List<VariableResponse> expectedList = List.of(
                JsonFileReader.readJson(objectMapper, GET_ALL_VARIABLES_RESPONSE_JSON, VariableResponse[].class)
        );
        when(variableRepository.findAll()).thenReturn(Flux.fromIterable(variables));

        StepVerifier.create(variableService.getAllVariables())
                    .recordWith(ArrayList::new)
                    .expectNextCount(variables.size())
                    .consumeRecordedWith(actualList -> assertThat(actualList).containsExactlyInAnyOrderElementsOf(expectedList))
                    .verifyComplete();
    }

    @Test
    void getAllVariables_returnsEmptyWhenNoVariablesExist() {
        when(variableRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(variableService.getAllVariables())
                    .verifyComplete();

    }
}
