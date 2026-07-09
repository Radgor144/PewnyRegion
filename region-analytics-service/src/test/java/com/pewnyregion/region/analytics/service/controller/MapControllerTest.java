package com.pewnyregion.region.analytics.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pewnyregion.region.analytics.service.exception.GlobalExceptionHandler;
import com.pewnyregion.region.analytics.service.model.MapRequest;
import com.pewnyregion.region.analytics.service.model.MapResponse;
import com.pewnyregion.region.analytics.service.service.MapService;
import com.pewnyregion.region.analytics.service.utils.JsonFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static com.pewnyregion.region.analytics.service.utils.TestConstants.GET_MAP_COUNTY_SCORES_API_PATH;
import static com.pewnyregion.region.analytics.service.utils.TestConstants.GET_POST_MAP_COUNTY_SCORES_RESPONSE_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MapControllerTest {

    private MapController mapController;
    private WebTestClient webTestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MapService mapService;

    @BeforeEach
    void setUp() {
        mapController = new MapController(mapService);

        webTestClient = WebTestClient.bindToController(mapController)
                                     .controllerAdvice(new GlobalExceptionHandler())
                                     .build();
    }

    @Test
    public void getCountyScores_shouldReturnOk_whenRequestIsValid() throws IOException {
        List<MapResponse> expectedList = List.of(
                JsonFileReader.readJson(objectMapper, GET_POST_MAP_COUNTY_SCORES_RESPONSE_JSON, MapResponse[].class)
        );
        MapRequest mapRequest = new MapRequest(List.of("crimes", "population_in_thousands"), 2020, 2023);

        when(mapService.getMapData(mapRequest)).thenReturn(Flux.fromIterable(expectedList));

        webTestClient.post()
                     .uri(GET_MAP_COUNTY_SCORES_API_PATH)
                     .bodyValue(mapRequest)
                     .exchange()
                     .expectStatus().isOk()
                     .expectBodyList(MapResponse.class)
                     .isEqualTo(expectedList);

        verify(mapService).getMapData(mapRequest);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideValidBoundaryMapRequests")
    public void getCountyScores_shouldReturnOk_whenRequestIsAtBoundary(String testCaseName, MapRequest mapRequest) throws IOException {
        List<MapResponse> expectedList = List.of(
                JsonFileReader.readJson(objectMapper, GET_POST_MAP_COUNTY_SCORES_RESPONSE_JSON, MapResponse[].class)
        );

        when(mapService.getMapData(mapRequest)).thenReturn(Flux.fromIterable(expectedList));

        webTestClient.post()
                     .uri(GET_MAP_COUNTY_SCORES_API_PATH)
                     .bodyValue(mapRequest)
                     .exchange()
                     .expectStatus().isOk()
                     .expectBodyList(MapResponse.class)
                     .isEqualTo(expectedList);

        verify(mapService).getMapData(mapRequest);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidMapRequests")
    public void getCountyScores_shouldReturnBadRequest_whenRequestIsInvalid(String testCaseName, MapRequest mapRequest, String expectedDetailMessage) {
        webTestClient.post()
                     .uri(GET_MAP_COUNTY_SCORES_API_PATH)
                     .bodyValue(mapRequest)
                     .exchange()
                     .expectStatus().isBadRequest()
                     .expectHeader().contentType("application/problem+json")
                     .expectBody(ProblemDetail.class)
                     .consumeWith(result -> {
                         ProblemDetail response = assertProblemDetailBasics(result.getResponseBody(), 400, "Bad Request");

                         String[] expectedErrors = expectedDetailMessage.split(", ");
                         assertThat(response.getDetail()).contains(expectedErrors);
                     });
    }

    @Test
    public void getCountyScores_shouldReturnBadRequest_whenJsonIsMalformed() {
        String malformedJson = "{\"apiNames\": [\"test\"], \"yearFrom\": \"string\"}";

        webTestClient.post()
                     .uri(GET_MAP_COUNTY_SCORES_API_PATH)
                     .header("Content-Type", "application/json")
                     .bodyValue(malformedJson)
                     .exchange()
                     .expectStatus().isBadRequest()
                     .expectHeader().contentType("application/problem+json")
                     .expectBody(ProblemDetail.class)
                     .consumeWith(result -> assertProblemDetailBasics(result.getResponseBody(), 400, "Bad Request"));
    }

    @Test
    public void getCountyScores_shouldReturnBadRequest_whenRequestBodyIsMissing() {
        webTestClient.post()
                     .uri(GET_MAP_COUNTY_SCORES_API_PATH)
                     .exchange()
                     .expectStatus().isBadRequest()
                     .expectHeader().contentType("application/problem+json")
                     .expectBody(ProblemDetail.class)
                     .consumeWith(result -> assertProblemDetailBasics(result.getResponseBody(), 400, "Bad Request"));
    }

    private ProblemDetail assertProblemDetailBasics(ProblemDetail response, int expectedStatus, String expectedTitle) {
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(expectedStatus);
        assertThat(response.getTitle()).isEqualTo(expectedTitle);
        assertThat(response.getInstance()).isNotNull();
        assertThat(response.getInstance().getPath()).isEqualTo(GET_MAP_COUNTY_SCORES_API_PATH);
        return response;
    }

    private static Stream<Arguments> provideValidBoundaryMapRequests() {
        return Stream.of(
                Arguments.of(
                        "apiNames has exactly 5 elements (max allowed)",
                        new MapRequest(List.of("one", "two", "three", "four", "five"), 2020, 2023)
                ),
                Arguments.of(
                        "apiNames has exactly 1 element (min allowed)",
                        new MapRequest(List.of("one"), 2020, 2023)
                ),
                Arguments.of(
                        "yearFrom is exactly the minimum allowed (2012)",
                        new MapRequest(List.of("one"), 2012, 2023)
                ),
                Arguments.of(
                        "yearTo is exactly the maximum allowed (2026)",
                        new MapRequest(List.of("one"), 2020, 2026)
                ),
                Arguments.of(
                        "yearFrom equals yearTo",
                        new MapRequest(List.of("one"), 2020, 2020)
                ),
                Arguments.of(
                        "yearFrom and yearTo are both at their extreme boundaries (2012 and 2026)",
                        new MapRequest(List.of("one"), 2012, 2026)
                )
        );
    }

    private static Stream<Arguments> provideInvalidMapRequests() {
        return Stream.of(
                Arguments.of(
                        "Too many API names (more than 5)",
                        new MapRequest(List.of("one", "two", "three", "four", "five", "six"), 2020, 2023),
                        "apiNames: Maximum 5 variables allowed"
                ),
                Arguments.of(
                        "yearFrom is one below the minimum allowed (2011)",
                        new MapRequest(List.of("one"), 2011, 2023),
                        "yearFrom: yearFrom cannot be earlier than 2012"
                ),
                Arguments.of(
                        "yearTo is one above the maximum allowed (2027)",
                        new MapRequest(List.of("one"), 2020, 2027),
                        "yearTo: yearTo cannot be later than 2026"
                ),
                Arguments.of(
                        "yearFrom is earlier than minimum allowed (2012)",
                        new MapRequest(List.of("one", "two", "three", "four", "five"), 2000, 2023),
                        "yearFrom: yearFrom cannot be earlier than 2012"
                ),
                Arguments.of(
                        "yearFrom is later than maximum (2026) and greater than yearTo",
                        new MapRequest(List.of("one", "two", "three", "four", "five"), 2200, 2023),
                        "yearFrom: yearFrom cannot be later than 2026, yearRangeValid: yearFrom must be less than or equal to yearTo"
                ),
                Arguments.of(
                        "yearTo is earlier than minimum (2012) and less than yearFrom",
                        new MapRequest(List.of("one", "two", "three", "four", "five"), 2020, 2000),
                        "yearTo: yearTo cannot be earlier than 2012, yearRangeValid: yearFrom must be less than or equal to yearTo"
                ),
                Arguments.of(
                        "yearTo is later than maximum allowed (2026)",
                        new MapRequest(List.of("one", "two", "three", "four", "five"), 2020, 2200),
                        "yearTo: yearTo cannot be later than 2026"
                ),
                Arguments.of(
                        "Both years are earlier than minimum allowed (2012)",
                        new MapRequest(List.of("one", "two", "three", "four", "five"), 2000, 2000),
                        "yearTo: yearTo cannot be earlier than 2012, yearFrom: yearFrom cannot be earlier than 2012"
                ),
                Arguments.of(
                        "Both years are later than maximum allowed (2026)",
                        new MapRequest(List.of("one", "two", "three", "four", "five"), 2200, 2200),
                        "yearFrom: yearFrom cannot be later than 2026, yearTo: yearTo cannot be later than 2026"
                ),
                Arguments.of(
                        "Valid year limits, but yearFrom is greater than yearTo",
                        new MapRequest(List.of("one", "two"), 2022, 2020),
                        "yearRangeValid: yearFrom must be less than or equal to yearTo"
                ),
                Arguments.of(
                        "API names list is empty",
                        new MapRequest(List.of(), 2020, 2023),
                        "apiNames: apiNames cannot be empty"
                ),
                Arguments.of(
                        "yearFrom is null",
                        new MapRequest(List.of("one", "two"), null, 2020),
                        "yearFrom: yearFrom is required"
                ),
                Arguments.of(
                        "yearTo is null",
                        new MapRequest(List.of("one", "two"), 2020, null),
                        "yearTo: yearTo is required"
                ),
                Arguments.of(
                        "Both yearFrom and yearTo are missing (null)",
                        new MapRequest(List.of("one", "two"), null, null),
                        "yearFrom: yearFrom is required, yearTo: yearTo is required"
                ),
                Arguments.of(
                        "variables are missing (null)",
                        new MapRequest(null, null, null),
                        "yearTo: yearTo is required, apiNames: apiNames cannot be empty, yearFrom: yearFrom is required"
                )
        );
    }
}
