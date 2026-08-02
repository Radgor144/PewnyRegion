package com.pewnyregion.region.data.service.integration;

import com.pewnyregion.region.data.service.config.AbstractIntegrationTest;
import com.pewnyregion.region.data.service.model.MapRequest;
import com.pewnyregion.region.data.service.model.MapResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static com.pewnyregion.region.data.service.utils.TestConstants.GET_MAP_COUNTY_SCORES_API_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class MapCountyScoresIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        runSqlScript("testdata/map-county-scores-test-data.sql");
    }

    @Test
    void shouldReturnAggregatedScores_whenMultipleVariablesRequestedAcrossFullYearRange() {
        MapRequest request = new MapRequest(List.of("crimes", "gross_salary"), 2015, 2018);

        List<MapResponse> responses = postAndExpectStatus(request, HttpStatus.OK);

        assertThat(responses)
                .extracting(MapResponse::countyId, MapResponse::countyName, MapResponse::score)
                .containsExactlyInAnyOrder(
                        tuple("011212006000", "Powiat krakowski", 53.64),
                        tuple("011212008000", "Powiat miechowski", 46.38),
                        tuple("011212019000", "Powiat wielicki", 49.40)
                );
    }

    @Test
    void shouldReturnAggregatedScores_whenSingleVariableRequestedForPartialYearRange() {
        MapRequest request = new MapRequest(List.of("crimes"), 2017, 2018);

        List<MapResponse> responses = postAndExpectStatus(request, HttpStatus.OK);

        assertThat(responses)
                .extracting(MapResponse::countyId, MapResponse::countyName, MapResponse::score)
                .containsExactlyInAnyOrder(
                        tuple("011212006000", "Powiat krakowski", 56.52),
                        tuple("011212008000", "Powiat miechowski", 53.63),
                        tuple("011212019000", "Powiat wielicki", 30.20)
                );
    }

    @Test
    void shouldReturnEmptyList_whenNoRecordsMatchRequestedYearRange() {
        MapRequest request = new MapRequest(List.of("crimes"), 2012, 2013);

        List<MapResponse> responses = postAndExpectStatus(request, HttpStatus.OK);

        assertThat(responses).isEmpty();
    }

    @Test
    void shouldReturnBadRequest_whenApiNameDoesNotExistInDatabase() {
        MapRequest request = new MapRequest(List.of("crimes", "unknown_variable"), 2015, 2018);

        ProblemDetail problem = postAndExpectProblem(request);

        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getDetail()).contains("Invalid names");
    }

    @Test
    void shouldReturnBadRequest_whenApiNamesListIsEmpty() {
        MapRequest request = new MapRequest(List.of(), 2015, 2018);

        ProblemDetail problem = postAndExpectProblem(request);

        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getDetail()).contains("apiNames cannot be empty");
    }

    private ProblemDetail postAndExpectProblem(MapRequest request) {
        return webTestClient.post()
                            .uri(GET_MAP_COUNTY_SCORES_API_PATH)
                            .bodyValue(request)
                            .exchange()
                            .expectStatus().isBadRequest()
                            .expectHeader().contentType("application/problem+json")
                            .expectBody(ProblemDetail.class)
                            .returnResult()
                            .getResponseBody();
    }

    private List<MapResponse> postAndExpectStatus(MapRequest request, HttpStatus expectedStatus) {
        return webTestClient.post()
                            .uri(GET_MAP_COUNTY_SCORES_API_PATH)
                            .bodyValue(request)
                            .exchange()
                            .expectStatus().isEqualTo(expectedStatus)
                            .expectBodyList(MapResponse.class)
                            .returnResult()
                            .getResponseBody();
    }
}
