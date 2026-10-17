package com.pewnyregion.region.data.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pewnyregion.region.data.service.client.BdlApiClient;
import com.pewnyregion.region.data.service.entity.CountyEntity;
import com.pewnyregion.region.data.service.model.BdlRawDataResponse;
import com.pewnyregion.region.data.service.model.ImportSummary;
import com.pewnyregion.region.data.service.repository.CountyRepository;
import com.pewnyregion.region.data.service.utils.JsonFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.pewnyregion.region.data.service.utils.TestConstants.BDL_BY_UNIT_CRIMES_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BdlDataImportServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private BdlDataImportService bdlDataImportService;
    private BdlRawDataResponse mockApiResponse;

    @Mock
    private VariableService variableService;
    @Mock
    private BdlApiClient bdlApiClient;
    @Mock
    private BdlDataPersistenceService persistenceService;
    @Mock
    private CountyRepository countyRepository;

    @BeforeEach
    void setUp() throws IOException {
        bdlDataImportService = new BdlDataImportService(variableService, bdlApiClient, persistenceService, countyRepository);
        mockApiResponse = JsonFileReader.readJson(OBJECT_MAPPER, BDL_BY_UNIT_CRIMES_JSON, BdlRawDataResponse.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideChunkTestCases")
    void runFullImport_shouldHandleChunksCorrectly(String testCaseName, List<Integer> variableIds, int expectedChunks) {
        CountyEntity county = createCounty("011212006000", "Powiat krakowski");

        when(countyRepository.findAll()).thenReturn(Flux.just(county));
        when(variableService.getAllVariableIds()).thenReturn(Mono.just(variableIds));
        when(bdlApiClient.fetchRawData(anyString(), anyList())).thenReturn(Mono.just(mockApiResponse));
        when(persistenceService.saveImportedData(anyString(), any())).thenReturn(Mono.just(mockApiResponse));

        assertImportSummary(bdlDataImportService.runFullImport(), expectedChunks);
    }

    @Test
    void runTargetedImport_shouldSuccessfullyImportTargetedDataAndReturnSummary() {
        CountyEntity county = createCounty("011212006000", "Powiat krakowski");
        List<String> apiNames = List.of("crimes", "unemployment");
        List<Integer> years = List.of(2023, 2024, 2025);
        List<Integer> variableIds = List.of(58559, 1749155);

        when(countyRepository.findAll()).thenReturn(Flux.just(county));
        when(variableService.getVariableIdsByApiNames(apiNames)).thenReturn(Mono.just(variableIds));
        when(bdlApiClient.fetchTargetedData(anyString(), anyList(), any())).thenReturn(Mono.just(mockApiResponse));
        when(persistenceService.saveImportedData(anyString(), any())).thenReturn(Mono.just(mockApiResponse));

        assertImportSummary(bdlDataImportService.runTargetedImport(apiNames, years), 1);
    }

    @Test
    void runFullImport_shouldContinueWhenTaskFailsDueToApiErrorExhaustingRetries() {
        CountyEntity county = createCounty("011212006000", "Powiat krakowski");
        List<Integer> variableIds = List.of(1, 2);
        AtomicInteger attemptCounter = new AtomicInteger(0);

        when(countyRepository.findAll()).thenReturn(Flux.just(county));
        when(variableService.getAllVariableIds()).thenReturn(Mono.just(variableIds));
        when(bdlApiClient.fetchRawData(anyString(), anyList()))
                .thenReturn(Mono.defer(() -> {
                    attemptCounter.incrementAndGet();
                    return Mono.error(new RuntimeException("API connection failed"));
                }));

        StepVerifier.withVirtualTime(() -> bdlDataImportService.runFullImport())
                    .expectSubscription()
                    .thenAwait(Duration.ofSeconds(15))
                    .assertNext(summary -> {
                        assertThat(summary).isNotNull();
                        assertThat(summary.processedChunks()).isEqualTo(0);
                    })
                    .verifyComplete();

        assertThat(attemptCounter.get()).isEqualTo(4);
    }

    @Test
    void runFullImport_shouldSucceedAfterRetriesWhenApiFailsTemporarily() {
        CountyEntity county = createCounty("011212006000", "Powiat krakowski");
        List<Integer> variableIds = List.of(1, 2);
        AtomicInteger attemptCounter = new AtomicInteger(0);

        when(countyRepository.findAll()).thenReturn(Flux.just(county));
        when(variableService.getAllVariableIds()).thenReturn(Mono.just(variableIds));
        when(bdlApiClient.fetchRawData(anyString(), anyList()))
                .thenReturn(Mono.defer(() -> {
                    int attempt = attemptCounter.incrementAndGet();
                    if (attempt < 3) {
                        return Mono.error(new RuntimeException("Temporary API failure"));
                    }
                    return Mono.just(mockApiResponse);
                }));
        when(persistenceService.saveImportedData(anyString(), any())).thenReturn(Mono.just(mockApiResponse));

        StepVerifier.withVirtualTime(() -> bdlDataImportService.runFullImport())
                    .expectSubscription()
                    .thenAwait(Duration.ofSeconds(10))
                    .assertNext(summary -> {
                        assertThat(summary).isNotNull();
                        assertThat(summary.processedChunks()).isEqualTo(1);
                    })
                    .verifyComplete();

        assertThat(attemptCounter.get()).isEqualTo(3);
        verify(persistenceService, times(1)).saveImportedData(anyString(), any());
    }

    @Test
    void runFullImport_shouldProcessMultipleCountiesInOrderAndHandleDistinctData() {
        List<Integer> variableIds = List.of(1, 2);

        CountyEntity county1 = createCounty("010000000000", "Powiat warszawski");
        CountyEntity county2 = createCounty("020000000000", "Powiat poznański");
        CountyEntity county3 = createCounty("030000000000", "Powiat gdański");

        when(countyRepository.findAll()).thenReturn(Flux.just(county1, county2, county3));
        when(variableService.getAllVariableIds()).thenReturn(Mono.just(variableIds));

        when(bdlApiClient.fetchRawData(eq("010000000000"), eq(variableIds))).thenReturn(Mono.just(mockApiResponse));
        when(bdlApiClient.fetchRawData(eq("020000000000"), eq(variableIds))).thenReturn(Mono.just(mockApiResponse));
        when(bdlApiClient.fetchRawData(eq("030000000000"), eq(variableIds))).thenReturn(Mono.just(mockApiResponse));

        when(persistenceService.saveImportedData(anyString(), any())).thenReturn(Mono.just(mockApiResponse));

        assertImportSummary(bdlDataImportService.runFullImport(), 3);

        InOrder inOrder = Mockito.inOrder(bdlApiClient, persistenceService);

        inOrder.verify(bdlApiClient).fetchRawData(eq("010000000000"), eq(variableIds));
        inOrder.verify(persistenceService).saveImportedData(eq("010000000000"), eq(mockApiResponse));

        inOrder.verify(bdlApiClient).fetchRawData(eq("020000000000"), eq(variableIds));
        inOrder.verify(persistenceService).saveImportedData(eq("020000000000"), eq(mockApiResponse));

        inOrder.verify(bdlApiClient).fetchRawData(eq("030000000000"), eq(variableIds));
        inOrder.verify(persistenceService).saveImportedData(eq("030000000000"), eq(mockApiResponse));
    }

    @Test
    void runFullImport_shouldContinueProcessingOtherCountiesWhenOneFailsAfterRetries() {
        List<Integer> variableIds = List.of(1, 2);

        CountyEntity failingCounty = createCounty("010000000000", "Powiat awaryjny");
        CountyEntity successCounty = createCounty("020000000000", "Powiat stabilny");

        when(countyRepository.findAll()).thenReturn(Flux.just(failingCounty, successCounty));
        when(variableService.getAllVariableIds()).thenReturn(Mono.just(variableIds));

        when(bdlApiClient.fetchRawData(eq("010000000000"), anyList()))
                .thenReturn(Mono.error(new RuntimeException("Persistent API failure")));

        when(bdlApiClient.fetchRawData(eq("020000000000"), anyList()))
                .thenReturn(Mono.just(mockApiResponse));
        when(persistenceService.saveImportedData(eq("020000000000"), any()))
                .thenReturn(Mono.just(mockApiResponse));

        StepVerifier.withVirtualTime(() -> bdlDataImportService.runFullImport())
                    .expectSubscription()
                    .thenAwait(Duration.ofSeconds(15))
                    .assertNext(summary -> {
                        assertThat(summary).isNotNull();
                        assertThat(summary.processedChunks()).isEqualTo(1);
                    })
                    .verifyComplete();

        verify(persistenceService, times(1)).saveImportedData(eq("020000000000"), any());
        verify(persistenceService, times(0)).saveImportedData(eq("010000000000"), any());
    }

    private static Stream<Arguments> provideChunkTestCases() {
        return Stream.of(
                Arguments.of(
                        "Small variable list fitting in 1 chunk (<= 10 elements)",
                        List.of(1, 2),
                        1
                ),
                Arguments.of(
                        "Large variable list exceeding chunk size (> 10 elements)",
                        List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                        2
                )
        );
    }

    private void assertImportSummary(Mono<ImportSummary> importStream, int expectedChunks) {
        StepVerifier.create(importStream)
                    .assertNext(summary -> {
                        assertThat(summary).isNotNull();
                        assertThat(summary.processedChunks()).isEqualTo(expectedChunks);
                    })
                    .verifyComplete();
    }

    private CountyEntity createCounty(String id, String name) {
        return CountyEntity.builder().id(id).name(name).build();
    }
}
