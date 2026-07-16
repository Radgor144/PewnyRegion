package com.pewnyregion.region.analytics.service.integration;

import com.pewnyregion.region.analytics.service.config.AbstractIntegrationTest;
import com.pewnyregion.region.analytics.service.model.MapRequest;
import com.pewnyregion.region.analytics.service.model.MapResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static com.pewnyregion.region.analytics.service.utils.TestConstants.GET_MAP_COUNTY_SCORES_API_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@AutoConfigureWebTestClient
class MapCountyScoresIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        runSqlScript("testdata/init-test-data.sql");
    }

    @Test
    void shouldReturnCountyScores_forFullPipeline_whenRequestIsValid() {
        MapRequest request = new MapRequest(List.of("crimes", "gross_salary"), 2015, 2018);

        List<MapResponse> actual = postAndExpectStatus(request, HttpStatus.OK);

        assertThat(actual).isNotEmpty();
        assertThat(actual)
                .hasSize(3)
                .extracting(MapResponse::countyId, MapResponse::countyName, MapResponse::score)
                .containsExactlyInAnyOrder(
                        tuple("011212006000", "Powiat krakowski", 53.64),
                        tuple("011212008000", "Powiat miechowski", 46.38),
                        tuple("011212019000", "Powiat wielicki", 49.40)
                );
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