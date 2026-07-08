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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VariableControllerTest {

    public static final String PATH_TO_FILE = "src/test/resources/getAllVariablesResponse.json";
    public static final String GET_VARIABLES_PATH = "/api/variables";

    private VariableController variableController;
    private ObjectMapper objectMapper = new ObjectMapper();
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
                JsonFileReader.readJson(objectMapper, PATH_TO_FILE, VariableResponse[].class)
        );

        when(variableService.getAllVariables()).thenReturn(Flux.fromIterable(expectedList));

        webTestClient.get()
                     .uri(GET_VARIABLES_PATH)
                     .exchange()
                     .expectStatus().isOk()
                     .expectBodyList(VariableResponse.class)
                     .hasSize(expectedList.size())
                     .isEqualTo(expectedList);
    }
}
