package com.pewnyregion.region.data.service.controller;

import com.pewnyregion.region.data.service.exception.GlobalExceptionHandler;
import com.pewnyregion.region.data.service.model.CountyDetailsResponse;
import com.pewnyregion.region.data.service.model.VariableDetail;
import com.pewnyregion.region.data.service.model.YearlyData;
import com.pewnyregion.region.data.service.service.CountyDetailsService;
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
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.pewnyregion.region.data.service.utils.TestConstants.GET_COUNTY_DETAILS_API_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountyDetailsControllerTest {

    private static final String TERYT_CODE = "1234";
    private static final String API_NAMES_CANNOT_BE_EMPTY = "apiNames: apiNames cannot be empty";
    private static final String TERYT_CODE_IS_REQUIRED = "terytCode: terytCode is required";

    private WebTestClient webTestClient;

    @Mock
    private CountyDetailsService countyDetailsService;

    @BeforeEach
    void setUp() {
        CountyDetailsController countyDetailsController = new CountyDetailsController(countyDetailsService);
        webTestClient = WebTestClient.bindToController(countyDetailsController)
                                     .controllerAdvice(new GlobalExceptionHandler())
                                     .build();
    }

    @Test
    void getCountyDetails_shouldReturnOk_whenRequestIsValid() {
        List<String> apiNames = List.of("flat_price_primary", "crimes_total", "gross_salary");
        CountyDetailsResponse expectedResponse = new CountyDetailsResponse(
                TERYT_CODE,
                List.of(new VariableDetail("flat_price_primary", List.of(new YearlyData(2023, 6320.0, 6331.96))),
                        new VariableDetail("crimes_total", List.of(new YearlyData(2023, 1773.0, 2084.25))),
                        new VariableDetail("gross_salary", List.of(new YearlyData(2023, 5678.11, 6560.32))))
        );

        when(countyDetailsService.getCountyDetails(TERYT_CODE, apiNames)).thenReturn(Mono.just(expectedResponse));

        getCountyDetails(TERYT_CODE, apiNames)
                .expectStatus().isOk()
                .expectBody(CountyDetailsResponse.class)
                .isEqualTo(expectedResponse);

        verify(countyDetailsService).getCountyDetails(TERYT_CODE, apiNames);
    }

    @Test
    void getCountyDetails_shouldReturnOk_whenServiceReturnsEmptyVariableDetails() {
        List<String> apiNames = List.of("population_in_thousands", "population_density");
        CountyDetailsResponse expectedResponse = new CountyDetailsResponse(TERYT_CODE, Collections.emptyList());

        when(countyDetailsService.getCountyDetails(TERYT_CODE, apiNames)).thenReturn(Mono.just(expectedResponse));

        getCountyDetails(TERYT_CODE, apiNames)
                .expectStatus().isOk()
                .expectBody(CountyDetailsResponse.class)
                .isEqualTo(expectedResponse);

        verify(countyDetailsService).getCountyDetails(TERYT_CODE, apiNames);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideValidCountyDetailsRequests")
    void getCountyDetails_shouldReturnOk_whenRequestIsValid_param(String testCaseName, String terytCode, List<String> apiNames) {
        List<VariableDetail> variableDetails = apiNames.stream()
                                                       .map(apiName -> new VariableDetail(
                                                               apiName,
                                                               List.of(new YearlyData(2023, 100.0, 50.0))
                                                       ))
                                                       .toList();

        CountyDetailsResponse expectedResponse = new CountyDetailsResponse(terytCode, variableDetails);

        when(countyDetailsService.getCountyDetails(terytCode, apiNames)).thenReturn(Mono.just(expectedResponse));

        getCountyDetails(terytCode, apiNames)
                .expectStatus().isOk()
                .expectBody(CountyDetailsResponse.class)
                .isEqualTo(expectedResponse);

        verify(countyDetailsService).getCountyDetails(terytCode, apiNames);
    }

    @Test
    void getCountyDetails_shouldReturnBadRequest_whenRequestParamsAreMissing() {
        webTestClient.get()
                     .uri(GET_COUNTY_DETAILS_API_PATH)
                     .exchange()
                     .expectStatus().isBadRequest()
                     .expectBody(ProblemDetail.class)
                     .consumeWith(result -> {
                         ProblemDetail response = assertProblemDetailBasics(result.getResponseBody(), 400);
                         assertThat(response.getDetail()).contains(TERYT_CODE_IS_REQUIRED);
                         assertThat(response.getDetail()).contains(API_NAMES_CANNOT_BE_EMPTY);
                     });
    }

    @Test
    void getCountyDetails_shouldReturnBadRequest_whenTerytCodeIsMissing() {
        webTestClient.get()
                     .uri(uriBuilder -> uriBuilder.path(GET_COUNTY_DETAILS_API_PATH)
                                                  .queryParam("apiNames", "population_in_thousands", "population_density")
                                                  .build())
                     .exchange()
                     .expectStatus().isBadRequest()
                     .expectBody(ProblemDetail.class)
                     .consumeWith(result -> assertBadRequestWithError(result.getResponseBody(), TERYT_CODE_IS_REQUIRED));
    }

    @Test
    void getCountyDetails_shouldReturnBadRequest_whenApiNamesAreMissing() {
        webTestClient.get()
                     .uri(uriBuilder -> uriBuilder.path(GET_COUNTY_DETAILS_API_PATH)
                                                  .queryParam("terytCode", TERYT_CODE)
                                                  .build())
                     .exchange()
                     .expectStatus().isBadRequest()
                     .expectBody(ProblemDetail.class)
                     .consumeWith(result -> assertBadRequestWithError(result.getResponseBody(), API_NAMES_CANNOT_BE_EMPTY));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidCountyDetailsRequests")
    void getCountyDetails_shouldReturnBadRequest_whenRequestIsInvalid(String testCaseName, String terytCode, List<String> apiNames, String expectedErrorFragment) {
        getCountyDetails(terytCode, apiNames)
                .expectStatus().isBadRequest()
                .expectBody(ProblemDetail.class)
                .consumeWith(result -> assertBadRequestWithError(result.getResponseBody(), expectedErrorFragment));
    }


    private void assertBadRequestWithError(ProblemDetail response, String expectedErrorFragment) {
        assertProblemDetailBasics(response, 400);
        assertThat(response.getDetail()).contains(expectedErrorFragment);
    }

    private ProblemDetail assertProblemDetailBasics(ProblemDetail response, int expectedStatus) {
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(expectedStatus);
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getInstance().getPath()).isEqualTo(GET_COUNTY_DETAILS_API_PATH);
        return response;
    }

    private WebTestClient.ResponseSpec getCountyDetails(String terytCode, List<String> apiNames) {
        return webTestClient.get()
                            .uri(uriBuilder -> uriBuilder.path(GET_COUNTY_DETAILS_API_PATH)
                                                         .queryParam("terytCode", terytCode)
                                                         .queryParam("apiNames", apiNames.toArray())
                                                         .build())
                            .exchange();
    }

    private static Stream<Arguments> provideValidCountyDetailsRequests() {
        return Stream.of(
                Arguments.of(
                        "Single variable API name",
                        "1465",
                        List.of("population_in_thousands")
                ),
                Arguments.of(
                        "Multiple variable API names",
                        "2261",
                        List.of("population_in_thousands", "population_density", "unemployment")
                ),
                Arguments.of(
                        "Variables with multiple BDL ID mappings (legacy & new)",
                        "2462",
                        List.of("crimes_total", "crime_detection_rate")
                ),
                Arguments.of(
                        "Mixed categories and units (Salary, Health, Labor)",
                        "1216",
                        List.of("gross_salary", "doctors", "unemployment")
                ),
                Arguments.of(
                        "Maximum allowed variables (Boundary limit of 10)",
                        "1465",
                        List.of(
                                "gross_salary", "doctors", "nurses",
                                "flat_price_avg", "flat_price_primary", "flat_price_secondary",
                                "unemployment", "population_in_thousands", "population_density", "crimes_total"
                        )
                )
        );
    }

    private static Stream<Arguments> provideInvalidCountyDetailsRequests() {
        return Stream.of(
                Arguments.of(
                        "Blank terytCode",
                        " ",
                        List.of("population_in_thousands"),
                        "terytCode must contain exactly 4 digits"
                ),
                Arguments.of(
                        "Empty terytCode",
                        "",
                        List.of("population_in_thousands"),
                        "terytCode is required"
                ),
                Arguments.of(
                        "terytCode with 3 digits",
                        "123",
                        List.of("population_in_thousands"),
                        "terytCode must contain exactly 4 digits"
                ),
                Arguments.of(
                        "terytCode with 5 digits",
                        "12345",
                        List.of("population_in_thousands"),
                        "terytCode must contain exactly 4 digits"
                ),
                Arguments.of(
                        "terytCode with letters",
                        "12AB",
                        List.of("population_in_thousands"),
                        "terytCode must contain exactly 4 digits"
                ),
                Arguments.of(
                        "Empty apiNames list",
                        TERYT_CODE,
                        Collections.emptyList(),
                        "apiNames cannot be empty"
                ),
                Arguments.of(
                        "apiNames with more than 10 values",
                        TERYT_CODE,
                        List.of("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9", "v10", "v11"),
                        "apiNames cannot contain more than 10 values"
                ),
                Arguments.of(
                        "apiNames contains blank value",
                        TERYT_CODE,
                        List.of(" ", "population_in_thousands"),
                        "apiNames cannot contain blank values"
                )
        );
    }
}