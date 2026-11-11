package com.pewnyregion.region.data.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pewnyregion.region.data.service.client.BdlApiClient;
import com.pewnyregion.region.data.service.component.BdlRawDataMapper;
import com.pewnyregion.region.data.service.entity.TerritorialUnitExternalIdEntity;
import com.pewnyregion.region.data.service.entity.VariableEntity;
import com.pewnyregion.region.data.service.entity.VariableExternalIdEntity;
import com.pewnyregion.region.data.service.model.BdlRawDataResponse;
import com.pewnyregion.region.data.service.model.StatisticalDataImportRecord;
import com.pewnyregion.region.data.service.model.TargetedImportRequest;
import com.pewnyregion.region.data.service.repository.TerritorialUnitExternalIdRepository;
import com.pewnyregion.region.data.service.repository.TerritorialUnitRepository;
import com.pewnyregion.region.data.service.repository.VariableExternalIdRepository;
import com.pewnyregion.region.data.service.repository.VariableRepository;
import com.pewnyregion.region.data.service.utils.JsonFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.pewnyregion.region.data.service.utils.TestConstants.BDL_BY_UNIT_CRIMES_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BdlStatisticalDataImportServiceTest {

    private static final String IMPORT_JOB_ID = "import-job";
    private static final String PROVIDER = "BDL";
    private static final String COUNTY_LEVEL = "COUNTY";
    private static final String TERYT_CODE = "011212006000";
    private static final StatisticalDataImportRecord RECORD =
            new StatisticalDataImportRecord(1L, 1L, 2023, 10.0);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private BdlStatisticalDataImportService importService;
    private BdlRawDataResponse mockApiResponse;

    @Mock
    private BdlApiClient bdlApiClient;
    @Mock
    private BdlRawDataMapper bdlRawDataMapper;
    @Mock
    private StatisticalDataPersistenceService persistenceService;
    @Mock
    private TerritorialUnitRepository territorialUnitRepository;
    @Mock
    private TerritorialUnitExternalIdRepository territorialUnitExternalIdRepository;
    @Mock
    private VariableRepository variableRepository;
    @Mock
    private VariableExternalIdRepository variableExternalIdRepository;

    @BeforeEach
    void setUp() throws IOException {
        importService = new BdlStatisticalDataImportService(bdlApiClient,
                                                            bdlRawDataMapper,
                                                            persistenceService,
                                                            territorialUnitRepository,
                                                            territorialUnitExternalIdRepository,
                                                            variableRepository,
                                                            variableExternalIdRepository);
        mockApiResponse = JsonFileReader.readJson(OBJECT_MAPPER, BDL_BY_UNIT_CRIMES_JSON, BdlRawDataResponse.class);
        lenient().when(bdlRawDataMapper.map(any(), anyList(), eq(mockApiResponse))).thenReturn(List.of(RECORD));
        lenient().when(persistenceService.saveAll(eq(IMPORT_JOB_ID), any())).thenReturn(Mono.just(1));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideChunkTestCases")
    void importAllCounties_shouldHandleChunksCorrectlyAndVerifyContent(String testCaseName,
                                                                       List<Integer> variableIds,
                                                                       List<List<Integer>> expectedChunks) {
        mockFullImportWithVariableIds(List.of(createCountyMapping(TERYT_CODE)), variableIds);
        when(bdlApiClient.fetchRawData(any(), anyList())).thenReturn(Mono.just(mockApiResponse));

        assertImportedRecords(importService.importAllCounties(IMPORT_JOB_ID), expectedChunks.size());

        ArgumentCaptor<List<Integer>> chunkCaptor = ArgumentCaptor.captor();
        verify(bdlApiClient, times(expectedChunks.size())).fetchRawData(eq(TERYT_CODE), chunkCaptor.capture());
        assertThat(chunkCaptor.getAllValues()).isEqualTo(expectedChunks);
    }

    @Test
    void importTargetedCounties_shouldSuccessfullyImportTargetedDataAndReturnCount() {
        List<String> apiNames = List.of("crimes_total", "unemployment");
        List<Integer> years = List.of(2023, 2024, 2025);
        mockCountyMappings(List.of(createCountyMapping(TERYT_CODE)));
        when(variableRepository.findByApiNameIn(apiNames)).thenReturn(Flux.just(
                createVariable(1, "crimes_total"),
                createVariable(2, "unemployment")
        ));
        when(variableExternalIdRepository.findAllByVariableIdInAndProvider(List.of(1, 2), PROVIDER))
                .thenReturn(Flux.just(createVariableMapping(1, 58559), createVariableMapping(2, 1749155)));
        when(bdlApiClient.fetchTargetedData(eq(TERYT_CODE), eq(List.of(58559, 1749155)), eq(years)))
                .thenReturn(Mono.just(mockApiResponse));

        assertImportedRecords(importService.importTargetedCounties(new TargetedImportRequest(apiNames, years), IMPORT_JOB_ID), 1);
        verify(bdlApiClient, Mockito.never()).fetchRawData(any(), anyList());
    }

    @Test
    void importAllCounties_shouldFailWhenApiErrorExhaustsRetries() {
        mockFullImportWithVariableIds(List.of(createCountyMapping(TERYT_CODE)), List.of(1, 2));
        AtomicInteger attemptCounter = new AtomicInteger();
        when(bdlApiClient.fetchRawData(any(), anyList()))
                .thenReturn(Mono.defer(() -> {
                    attemptCounter.incrementAndGet();
                    return Mono.error(apiError("API connection failed"));
                }));

        StepVerifier.create(importService.importAllCounties(IMPORT_JOB_ID))
                    .expectErrorSatisfies(error -> {
                        assertThat(error.getMessage()).contains("Retries exhausted");
                        assertThat(error.getCause()).isInstanceOf(WebClientResponseException.class);
                    })
                    .verify();

        assertThat(attemptCounter.get()).isEqualTo(4);
        verifyNoInteractions(persistenceService);
    }

    @Test
    void importAllCounties_shouldSucceedAfterRetriesWhenApiFailsTemporarily() {
        mockFullImportWithVariableIds(List.of(createCountyMapping(TERYT_CODE)), List.of(1, 2));
        AtomicInteger attemptCounter = new AtomicInteger();
        when(bdlApiClient.fetchRawData(any(), anyList()))
                .thenReturn(Mono.defer(() -> {
                    int attempt = attemptCounter.incrementAndGet();
                    if (attempt < 3) {
                        return Mono.error(apiError("Temporary API failure"));
                    }
                    return Mono.just(mockApiResponse);
                }));

        assertImportedRecords(importService.importAllCounties(IMPORT_JOB_ID), 1);

        assertThat(attemptCounter.get()).isEqualTo(3);
        verify(persistenceService).saveAll(eq(IMPORT_JOB_ID), any());
    }

    @Test
    void importAllCounties_shouldStopWhenACountyFailsAfterRetries() {
        mockFullImportWithVariableIds(
                List.of(createCountyMapping("010000000000"), createCountyMapping("020000000000")),
                List.of(1, 2)
        );
        AtomicInteger attemptCounter = new AtomicInteger();
        when(bdlApiClient.fetchRawData(eq("010000000000"), anyList()))
                .thenReturn(Mono.defer(() -> {
                    attemptCounter.incrementAndGet();
                    return Mono.error(apiError("Persistent API failure"));
                }));

        StepVerifier.create(importService.importAllCounties(IMPORT_JOB_ID))
                    .expectErrorSatisfies(error -> {
                        assertThat(error.getMessage()).contains("Retries exhausted");
                        assertThat(error.getCause()).isInstanceOf(WebClientResponseException.class);
                    })
                    .verify();

        assertThat(attemptCounter.get()).isEqualTo(4);
        verify(bdlApiClient).fetchRawData(eq("010000000000"), anyList());
        verify(bdlApiClient, Mockito.never()).fetchRawData(eq("020000000000"), anyList());
        verifyNoInteractions(persistenceService);
    }

    @Test
    void importAllCounties_shouldProcessMultipleCountiesInOrder() {
        List<TerritorialUnitExternalIdEntity> counties = List.of(
                createCountyMapping("010000000000"),
                createCountyMapping("020000000000"),
                createCountyMapping("030000000000")
        );
        List<VariableExternalIdEntity> variables = createVariableMappings(List.of(1, 2));
        mockFullImportWithMappings(counties, variables);
        when(bdlApiClient.fetchRawData(any(), anyList())).thenReturn(Mono.just(mockApiResponse));

        assertImportedRecords(importService.importAllCounties(IMPORT_JOB_ID), 3);

        InOrder inOrder = Mockito.inOrder(bdlApiClient, bdlRawDataMapper, persistenceService);
        for (TerritorialUnitExternalIdEntity county : counties) {
            inOrder.verify(bdlApiClient).fetchRawData(eq(county.getExternalId()), eq(List.of(1, 2)));
            inOrder.verify(bdlRawDataMapper).map(eq(county), eq(variables), eq(mockApiResponse));
            inOrder.verify(persistenceService).saveAll(eq(IMPORT_JOB_ID), any());
        }
    }

    private static Stream<Arguments> provideChunkTestCases() {
        return Stream.of(
                Arguments.of(
                        "Small variable list fitting in 1 chunk (2 elements)",
                        List.of(1, 2),
                        List.of(List.of(1, 2))
                ),
                Arguments.of(
                        "Boundary case: exactly 10 elements (1 full chunk)",
                        List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
                        List.of(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
                ),
                Arguments.of(
                        "Boundary case: 11 elements (2 chunks: 10 + 1)",
                        List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
                        List.of(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), List.of(11))
                ),
                Arguments.of(
                        "Standard case: 12 elements (2 chunks: 10 + 2)",
                        List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                        List.of(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), List.of(11, 12))
                ),
                Arguments.of(
                        "Boundary case: exactly 20 elements (2 full chunks)",
                        List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20),
                        List.of(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), List.of(11, 12, 13, 14, 15, 16, 17, 18, 19, 20))
                ),
                Arguments.of(
                        "Boundary case: 21 elements (3 chunks: 10 + 10 + 1)",
                        List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21),
                        List.of(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), List.of(11, 12, 13, 14, 15, 16, 17, 18, 19, 20), List.of(21))
                )
        );
    }

    private void mockFullImportWithVariableIds(List<TerritorialUnitExternalIdEntity> counties, List<Integer> variableIds) {
        mockFullImportWithMappings(counties, createVariableMappings(variableIds));
    }

    private void mockFullImportWithMappings(List<TerritorialUnitExternalIdEntity> counties, List<VariableExternalIdEntity> variables) {
        mockCountyMappings(counties);
        mockVariableMappings(variables);
    }

    private void mockCountyMappings(List<TerritorialUnitExternalIdEntity> counties) {
        when(territorialUnitRepository.countByUnitLevelAndActive(COUNTY_LEVEL, true)).thenReturn(Mono.just((long) counties.size()));
        when(territorialUnitExternalIdRepository.findAllByProviderAndUnitLevel(PROVIDER, COUNTY_LEVEL))
                .thenReturn(Flux.fromIterable(counties));
    }

    private void mockVariableMappings(List<VariableExternalIdEntity> variables) {
        when(variableExternalIdRepository.findAllByProvider(PROVIDER)).thenReturn(Flux.fromIterable(variables));
    }

    private void assertImportedRecords(Mono<Integer> importStream, int expectedRecords) {
        StepVerifier.create(importStream)
                    .expectNext(expectedRecords)
                    .verifyComplete();
    }

    private List<VariableExternalIdEntity> createVariableMappings(List<Integer> variableIds) {
        return variableIds.stream().map(id -> createVariableMapping(id, id)).toList();
    }

    private TerritorialUnitExternalIdEntity createCountyMapping(String externalId) {
        TerritorialUnitExternalIdEntity mapping = new TerritorialUnitExternalIdEntity();
        mapping.setId((long) externalId.hashCode());
        mapping.setTerritorialUnitId((long) externalId.hashCode());
        mapping.setProvider(PROVIDER);
        mapping.setExternalId(externalId);
        mapping.setProviderLevel(COUNTY_LEVEL);
        return mapping;
    }

    private VariableExternalIdEntity createVariableMapping(int variableId, int externalId) {
        VariableExternalIdEntity mapping = new VariableExternalIdEntity();
        mapping.setId((long) variableId);
        mapping.setVariableId(variableId);
        mapping.setProvider(PROVIDER);
        mapping.setExternalId(String.valueOf(externalId));
        return mapping;
    }

    private VariableEntity createVariable(int id, String apiName) {
        return new VariableEntity(id, apiName, "category", "STIMULANT", false, "unit");
    }

    private WebClientResponseException apiError(String message) {
        return WebClientResponseException.create(503, message, HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);
    }
}
