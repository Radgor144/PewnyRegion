package com.pewnyregion.region.data.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.pewnyregion.region.data.service.model.BdlRawDataResponse;
import com.pewnyregion.region.data.service.model.BdlResponse;
import com.pewnyregion.region.data.service.utils.JsonFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.havingExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static com.pewnyregion.region.data.service.utils.TestConstants.BDL_BY_UNIT_CRIMES_JSON;
import static com.pewnyregion.region.data.service.utils.TestConstants.BDL_DATA_BY_UNIT_PATH;
import static com.pewnyregion.region.data.service.utils.TestConstants.BDL_TARGETED_BY_UNIT_CRIMES_JSON;
import static com.pewnyregion.region.data.service.utils.TestConstants.BDL_UNITS_PAGE_0_JSON;
import static com.pewnyregion.region.data.service.utils.TestConstants.BDL_UNITS_PATH;

@WireMockTest
class BdlApiClientTest {

    private static final String ID_PATH_PARAM = "id";
    private static final String FORMAT_PARAM = "format";
    private static final String FORMAT_VALUE = "json";
    private static final String VAR_ID_PARAM = "var-id";
    private static final String YEAR_PARAM = "year";
    private static final String PAGE_PARAM = "page";
    private static final String LEVEL_PARAM = "level";
    private static final String PAGE_SIZE_PARAM = "page-size";

    private static final String COUNTY_ID = "011212006000";
    private static final int PAGE = 0;
    private static final int LEVEL = 5;
    private static final int PAGE_SIZE = 100;
    private static final int TOO_MANY_REQUESTS = 429;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private BdlApiClient bdlApiClient;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wm) {
        bdlApiClient = new BdlApiClient(WebClient.builder()
                                                 .baseUrl(wm.getHttpBaseUrl())
                                                 .build());
    }

    @Test
    void fetchUnits_sendsExpectedQuery_andDeserializesBdlPayload() throws IOException {
        BdlResponse expected = JsonFileReader.readJson(OBJECT_MAPPER, BDL_UNITS_PAGE_0_JSON, BdlResponse.class);
        stubUnitsEndpoint(PAGE, LEVEL, PAGE_SIZE, readFile(BDL_UNITS_PAGE_0_JSON));

        verifySuccess(bdlApiClient.fetchUnits(PAGE, LEVEL, PAGE_SIZE), expected);
    }

    @Test
    void fetchRawData_sendsRepeatedVarIdParams_andCountyInPath() throws IOException {
        List<Integer> varIds = List.of(58559, 1749155);
        BdlRawDataResponse expected = JsonFileReader.readJson(OBJECT_MAPPER, BDL_BY_UNIT_CRIMES_JSON, BdlRawDataResponse.class);
        stubFetchRawDataEndpoint(COUNTY_ID, varIds, readFile(BDL_BY_UNIT_CRIMES_JSON));

        verifySuccess(bdlApiClient.fetchRawData(COUNTY_ID, varIds), expected);
    }

    @Test
    void fetchTargetedData_sendsVarIdAndYearParams_andCountyInPath() throws IOException {
        List<Integer> varIds = List.of(58559);
        List<Integer> years = List.of(2024);
        BdlRawDataResponse expected = JsonFileReader.readJson(OBJECT_MAPPER, BDL_TARGETED_BY_UNIT_CRIMES_JSON, BdlRawDataResponse.class);
        stubFetchTargetedDataEndpoint(COUNTY_ID, varIds, years, readFile(BDL_TARGETED_BY_UNIT_CRIMES_JSON));

        verifySuccess(bdlApiClient.fetchTargetedData(COUNTY_ID, varIds, years), expected);
    }

    @Test
    void fetchUnits_propagatesTooManyRequests() {
        stubUnitsEndpointWithError(TOO_MANY_REQUESTS);

        verifyError(bdlApiClient.fetchUnits(PAGE, LEVEL, PAGE_SIZE), WebClientResponseException.TooManyRequests.class);
    }

    @Test
    void fetchRawData_propagatesTooManyRequests() {
        stubDataByUnitEndpointWithError(COUNTY_ID, TOO_MANY_REQUESTS);

        verifyError(bdlApiClient.fetchRawData(COUNTY_ID, List.of(123)), WebClientResponseException.TooManyRequests.class);
    }

    @Test
    void fetchTargetedData_propagatesTooManyRequests() {
        stubDataByUnitEndpointWithError(COUNTY_ID, TOO_MANY_REQUESTS);

        verifyError(bdlApiClient.fetchTargetedData(COUNTY_ID, List.of(123), List.of(2024)), WebClientResponseException.TooManyRequests.class);
    }

    private void stubUnitsEndpoint(int page, int level, int pageSize, String responseBody) {
        stubFor(get(urlPathEqualTo(BDL_UNITS_PATH))
                .withQueryParam(FORMAT_PARAM, equalTo(FORMAT_VALUE))
                .withQueryParam(PAGE_PARAM, equalTo(String.valueOf(page)))
                .withQueryParam(LEVEL_PARAM, equalTo(String.valueOf(level)))
                .withQueryParam(PAGE_SIZE_PARAM, equalTo(String.valueOf(pageSize)))
                .willReturn(okJson(responseBody)));
    }

    private void stubFetchRawDataEndpoint(String countyId, List<Integer> varIds, String responseBody) {
        stubFor(get(urlPathTemplate(BDL_DATA_BY_UNIT_PATH))
                .withPathParam(ID_PATH_PARAM, equalTo(countyId))
                .withQueryParam(FORMAT_PARAM, equalTo(FORMAT_VALUE))
                .withQueryParam(VAR_ID_PARAM, havingExactly(toStringArray(varIds)))
                .willReturn(okJson(responseBody)));
    }

    private void stubFetchTargetedDataEndpoint(String countyId, List<Integer> varIds, List<Integer> years, String responseBody) {
        stubFor(get(urlPathTemplate(BDL_DATA_BY_UNIT_PATH))
                .withPathParam(ID_PATH_PARAM, equalTo(countyId))
                .withQueryParam(FORMAT_PARAM, equalTo(FORMAT_VALUE))
                .withQueryParam(VAR_ID_PARAM, havingExactly(toStringArray(varIds)))
                .withQueryParam(YEAR_PARAM, havingExactly(toStringArray(years)))
                .willReturn(okJson(responseBody)));
    }

    private void stubUnitsEndpointWithError(int status) {
        stubFor(get(urlPathEqualTo(BDL_UNITS_PATH))
                .willReturn(aResponse().withStatus(status)));
    }

    private void stubDataByUnitEndpointWithError(String countyId, int status) {
        stubFor(get(urlPathTemplate(BDL_DATA_BY_UNIT_PATH))
                .withPathParam(ID_PATH_PARAM, equalTo(countyId))
                .willReturn(aResponse().withStatus(status)));
    }

    private static String readFile(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    private static String[] toStringArray(List<Integer> values) {
        return values.stream()
                     .map(String::valueOf)
                     .toArray(String[]::new);
    }

    private static <T> void verifySuccess(Mono<T> mono, T expected) {
        StepVerifier.create(mono)
                    .expectNext(expected)
                    .verifyComplete();
    }

    private static void verifyError(Mono<?> mono, Class<? extends Throwable> errorType) {
        StepVerifier.create(mono)
                    .expectError(errorType)
                    .verify();
    }
}
