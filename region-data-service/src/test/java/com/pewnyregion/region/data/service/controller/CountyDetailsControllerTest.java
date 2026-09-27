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
    private static final String VARIABLE_IDS_CANNOT_BE_EMPTY = "bdlVariableIds: bdlVariableIds cannot be empty";
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
        List<Integer> variableIds = List.of(1645341, 60559, 60270);
        CountyDetailsResponse expectedResponse = new CountyDetailsResponse(
                TERYT_CODE,
                List.of(new VariableDetail(1645341, List.of(new YearlyData(2023, 12.5, 10.0))))
        );

        when(countyDetailsService.getCountyDetails(TERYT_CODE, variableIds)).thenReturn(Mono.just(expectedResponse));

        getCountyDetails(TERYT_CODE, variableIds)
                .expectStatus().isOk()
                .expectBody(CountyDetailsResponse.class)
                .isEqualTo(expectedResponse);

        verify(countyDetailsService).getCountyDetails(TERYT_CODE, variableIds);
    }

    @Test
    void getCountyDetails_shouldReturnOk_whenServiceReturnsEmptyVariableDetails() {
        List<Integer> variableIds = List.of(1645341, 60559);
        CountyDetailsResponse expectedResponse = new CountyDetailsResponse(TERYT_CODE, Collections.emptyList());

        when(countyDetailsService.getCountyDetails(TERYT_CODE, variableIds)).thenReturn(Mono.just(expectedResponse));

        getCountyDetails(TERYT_CODE, variableIds)
                .expectStatus().isOk()
                .expectBody(CountyDetailsResponse.class)
                .isEqualTo(expectedResponse);

        verify(countyDetailsService).getCountyDetails(TERYT_CODE, variableIds);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideValidCountyDetailsRequests")
    void getCountyDetails_shouldReturnOk_whenRequestIsValid(String testCaseName, String terytCode, List<Integer> variableIds) {
        CountyDetailsResponse expectedResponse = new CountyDetailsResponse(
                terytCode,
                List.of(new VariableDetail(variableIds.get(0), List.of(new YearlyData(2023, 100.0, 50.0))))
        );

        when(countyDetailsService.getCountyDetails(terytCode, variableIds)).thenReturn(Mono.just(expectedResponse));

        getCountyDetails(terytCode, variableIds)
                .expectStatus().isOk()
                .expectBody(CountyDetailsResponse.class)
                .isEqualTo(expectedResponse);

        verify(countyDetailsService).getCountyDetails(terytCode, variableIds);
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
                         assertThat(response.getDetail()).contains(VARIABLE_IDS_CANNOT_BE_EMPTY);
                     });
    }

    @Test
    void getCountyDetails_shouldReturnBadRequest_whenTerytCodeIsMissing() {
        webTestClient.get()
                     .uri(uriBuilder -> uriBuilder.path(GET_COUNTY_DETAILS_API_PATH)
                                                  .queryParam("bdlVariableIds", 1645341, 60559)
                                                  .build())
                     .exchange()
                     .expectStatus().isBadRequest()
                     .expectBody(ProblemDetail.class)
                     .consumeWith(result -> assertBadRequestWithError(result.getResponseBody(), TERYT_CODE_IS_REQUIRED));
    }

    @Test
    void getCountyDetails_shouldReturnBadRequest_whenBdlVariableIdsAreMissing() {
        webTestClient.get()
                     .uri(uriBuilder -> uriBuilder.path(GET_COUNTY_DETAILS_API_PATH)
                                                  .queryParam("terytCode", TERYT_CODE)
                                                  .build())
                     .exchange()
                     .expectStatus().isBadRequest()
                     .expectBody(ProblemDetail.class)
                     .consumeWith(result -> assertBadRequestWithError(result.getResponseBody(), VARIABLE_IDS_CANNOT_BE_EMPTY));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidCountyDetailsRequests")
    void getCountyDetails_shouldReturnBadRequest_whenRequestIsInvalid(String testCaseName, String terytCode, List<Integer> variableIds, String expectedErrorFragment) {
        getCountyDetails(terytCode, variableIds)
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

    private WebTestClient.ResponseSpec getCountyDetails(String terytCode, List<Integer> variableIds) {
        return webTestClient.get()
                            .uri(uriBuilder -> uriBuilder.path(GET_COUNTY_DETAILS_API_PATH)
                                                         .queryParam("terytCode", terytCode)
                                                         .queryParam("bdlVariableIds", variableIds.toArray())
                                                         .build())
                            .exchange();
    }

    private static Stream<Arguments> provideValidCountyDetailsRequests() {
        return Stream.of(
                Arguments.of(
                        "Single variable ID",
                        "1465",
                        List.of(1645341)
                ),
                Arguments.of(
                        "Multiple variable IDs (crimes, population, unemployment)",
                        "2261",
                        List.of(1645341, 60559, 60270)
                ),
                Arguments.of(
                        "Different terytCode with two variables",
                        "2462",
                        List.of(58559, 1749155)
                )
        );
    }

    private static Stream<Arguments> provideInvalidCountyDetailsRequests() {
        return Stream.of(
                Arguments.of(
                        "Blank terytCode",
                        " ",
                        List.of(1645341, 60559),
                        "terytCode must contain exactly 4 digits"
                ),
                Arguments.of(
                        "Empty terytCode",
                        "",
                        List.of(1645341, 60559),
                        "terytCode is required"
                ),
                Arguments.of(
                        "terytCode with 3 digits",
                        "123",
                        List.of(1645341, 60559),
                        "terytCode must contain exactly 4 digits"
                ),
                Arguments.of(
                        "terytCode with 5 digits",
                        "12345",
                        List.of(1645341, 60559),
                        "terytCode must contain exactly 4 digits"
                ),
                Arguments.of(
                        "terytCode with letters",
                        "12AB",
                        List.of(1645341, 60559),
                        "terytCode must contain exactly 4 digits"
                ),
                Arguments.of(
                        "Empty bdlVariableIds list",
                        TERYT_CODE,
                        Collections.emptyList(),
                        "bdlVariableIds cannot be empty"
                ),
                Arguments.of(
                        "bdlVariableIds with more than 10 values",
                        TERYT_CODE,
                        List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
                        "bdlVariableIds cannot contain more than 10 values"
                ),
                Arguments.of(
                        "bdlVariableIds contains zero",
                        TERYT_CODE,
                        List.of(0, 1645341),
                        "bdlVariableIds must contain positive numbers"
                ),
                Arguments.of(
                        "bdlVariableIds contains negative value",
                        TERYT_CODE,
                        List.of(-1, 1645341),
                        "bdlVariableIds must contain positive numbers"
                )
        );
    }
}