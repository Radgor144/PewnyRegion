package com.pewnyregion.region.data.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pewnyregion.region.data.service.client.BdlApiClient;
import com.pewnyregion.region.data.service.entity.CountyEntity;
import com.pewnyregion.region.data.service.model.BdlRawDataResponse;
import com.pewnyregion.region.data.service.repository.CountyRepository;
import com.pewnyregion.region.data.service.utils.JsonFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BdlDataImportServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TEST_COUNTY_ID = "011212006000";
    private static final String TEST_COUNTY_NAME = "Powiat krakowski";

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
    @Mock
    private CountyEntity county;

    @BeforeEach
    void setUp() throws IOException {
        bdlDataImportService = new BdlDataImportService(variableService, bdlApiClient, persistenceService, countyRepository);
        mockApiResponse = JsonFileReader.readJson(OBJECT_MAPPER, BDL_BY_UNIT_CRIMES_JSON, BdlRawDataResponse.class);

        when(countyRepository.findAll()).thenReturn(Flux.just(county));
        when(county.getId()).thenReturn(TEST_COUNTY_ID);
        when(county.getName()).thenReturn(TEST_COUNTY_NAME);
        lenient().when(persistenceService.saveImportedData(anyString(), any())).thenReturn(Mono.just(mockApiResponse));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideChunkTestCases")
    void runFullImport_shouldHandleChunksCorrectly(String testCaseName, List<Integer> variableIds, int expectedChunks) {
        when(variableService.getAllVariableIds()).thenReturn(Mono.just(variableIds));
        when(bdlApiClient.fetchRawData(anyString(), anyList())).thenReturn(Mono.just(mockApiResponse));

        StepVerifier.create(bdlDataImportService.runFullImport())
                    .assertNext(summary -> {
                        assertThat(summary).isNotNull();
                        assertThat(summary.processedChunks()).isEqualTo(expectedChunks);
                    })
                    .verifyComplete();
    }

    @Test
    void runTargetedImport_shouldSuccessfullyImportTargetedDataAndReturnSummary() {
        List<String> apiNames = List.of("crimes", "unemployment");
        List<Integer> years = List.of(2023, 2024, 2025);
        List<Integer> variableIds = List.of(58559, 1749155);

        when(variableService.getVariableIdsByApiNames(apiNames)).thenReturn(Mono.just(variableIds));
        when(bdlApiClient.fetchTargetedData(anyString(), anyList(), any())).thenReturn(Mono.just(mockApiResponse));

        StepVerifier.create(bdlDataImportService.runTargetedImport(apiNames, years))
                    .assertNext(summary -> {
                        assertThat(summary).isNotNull();
                        assertThat(summary.processedChunks()).isEqualTo(1);
                    })
                    .verifyComplete();
    }

    @Test
    void runFullImport_shouldContinueWhenTaskFailsDueToApiError() {
        List<Integer> variableIds = List.of(1, 2);
        AtomicInteger attemptCounter = new AtomicInteger(0);

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
}