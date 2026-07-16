package com.pewnyregion.region.analytics.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pewnyregion.region.analytics.service.model.VariableResponse;
import com.pewnyregion.region.analytics.service.service.VariableService;
import com.pewnyregion.region.analytics.service.utils.JsonFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

import static com.pewnyregion.region.analytics.service.utils.TestConstants.GET_ALL_VARIABLES_RESPONSE_JSON;
import static com.pewnyregion.region.analytics.service.utils.TestConstants.GET_VARIABLES_API_PATH;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VariableControllerTest {

    private final  ObjectMapper objectMapper = new ObjectMapper();
    private VariableController variableController;
    private WebTestClient webTestClient;

    @Mock
    private VariableService variableService;

    @BeforeEach
    void setUp() {
        variableController = new VariableController(variableService);
        webTestClient = WebTestClient.bindToController(variableController).build();
    }

    @Test
    public void getAllVariables() throws IOException {
        List<VariableResponse> expectedList = List.of(
                JsonFileReader.readJson(objectMapper, GET_ALL_VARIABLES_RESPONSE_JSON, VariableResponse[].class)
        );

        when(variableService.getAllVariables()).thenReturn(Flux.fromIterable(expectedList));

        webTestClient.get()
                     .uri(GET_VARIABLES_API_PATH)
                     .exchange()
                     .expectStatus().isOk()
                     .expectBodyList(VariableResponse.class)
                     .hasSize(expectedList.size())
                     .isEqualTo(expectedList);
    }
}
