package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.model.dto.NormalizationStatsDto;
import com.pewnyregion.region.data.service.repository.BdlDataRecordRepository;
import com.pewnyregion.region.data.service.repository.CountyVariableScoreRepository;
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

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NormalizationServiceTest {

    private static final Integer YEAR = 2023;
    private static final String COUNTY_ID = "0201011";
    private static final int VAR_ID = 1;

    private static final double RAW_VAL = 120.0;
    private static final double ADJ_VAL = 60.0;
    private static final double PERCENTILE = 0.6667;

    private static final double EXPECTED_STIM_SCORE = 66.67;
    private static final double EXPECTED_DESTIM_SCORE = 33.33;

    private NormalizationService normalizationService;

    @Mock
    private BdlDataRecordRepository dataRepository;
    @Mock
    private CountyVariableScoreRepository scoreRepository;

    @BeforeEach
    void setUp() {
        normalizationService = new NormalizationService(dataRepository, scoreRepository);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideValidStatsForScoring")
    void calculateForYears_savesCorrectScoreBasedOnDirection(String testCaseName, String direction, double expectedScore) {
        mockDataRepoWith(createStat(direction));
        mockSuccessfulScoreSave();

        StepVerifier.create(normalizationService.calculateAndSaveScoresForYears(List.of(YEAR)))
                    .assertNext(summary -> assertThat(summary.processedYears()).containsExactly(YEAR))
                    .verifyComplete();

        verifyScoreSaved(expectedScore);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideYearLists")
    void calculateForYears_processesInputYearsCorrectly(String testCaseName, List<Integer> inputYears, List<Integer> expectedProcessedYears) {
        when(dataRepository.getNormalizationStatsForYear(any())).thenReturn(Flux.empty());

        StepVerifier.create(normalizationService.calculateAndSaveScoresForYears(inputYears))
                    .assertNext(summary -> assertThat(summary.processedYears()).containsExactlyElementsOf(expectedProcessedYears))
                    .verifyComplete();

        for (Integer expectedYear : expectedProcessedYears) {
            verify(dataRepository, times(1)).getNormalizationStatsForYear(expectedYear);
        }
    }

    @Test
    void calculateForAllYears_fetchesAndCalculatesScores() {
        when(dataRepository.findDistinctYears()).thenReturn(Flux.just(YEAR));
        mockDataRepoWith(createStat("STIMULANT"));
        mockSuccessfulScoreSave();

        StepVerifier.create(normalizationService.calculateAndSaveScoresForAllYears())
                    .assertNext(summary -> assertThat(summary.processedYears()).containsExactly(YEAR))
                    .verifyComplete();

        verify(dataRepository).findDistinctYears();
        verifyScoreSaved(EXPECTED_STIM_SCORE);
    }

    @Test
    void calculateForAllYears_deduplicatesSourceYears() {
        when(dataRepository.findDistinctYears()).thenReturn(Flux.just(YEAR, YEAR));
        when(dataRepository.getNormalizationStatsForYear(YEAR)).thenReturn(Flux.empty());

        StepVerifier.create(normalizationService.calculateAndSaveScoresForAllYears())
                    .assertNext(summary -> assertThat(summary.processedYears()).containsExactly(YEAR))
                    .verifyComplete();

        verify(dataRepository, times(1)).getNormalizationStatsForYear(YEAR);
    }

    private static Stream<Arguments> provideValidStatsForScoring() {
        return Stream.of(
                Arguments.of("Saves inverted score for DESTIMULANT", "DESTIMULANT", EXPECTED_DESTIM_SCORE),
                Arguments.of("Saves normal score for STIMULANT", "STIMULANT", EXPECTED_STIM_SCORE)
        );
    }

    private static Stream<Arguments> provideYearLists() {
        return Stream.of(
                Arguments.of("Deduplicates input years", List.of(2023, 2023), List.of(2023)),
                Arguments.of("Returns sorted years", List.of(2023, 2020, 2021), List.of(2020, 2021, 2023)),
                Arguments.of("Deduplicates and sorts input years", List.of(2023, 2020, 2021, 2020, 2023), List.of(2020, 2021, 2023))
        );
    }

    private NormalizationStatsDto createStat(String direction) {
        return new NormalizationStatsDto(COUNTY_ID, VAR_ID, YEAR, RAW_VAL, ADJ_VAL, direction, PERCENTILE);
    }

    private void mockDataRepoWith(NormalizationStatsDto stat) {
        when(dataRepository.getNormalizationStatsForYear(YEAR)).thenReturn(Flux.just(stat));
    }

    private void mockSuccessfulScoreSave() {
        when(scoreRepository.upsertScore(any(), any(), any(), any(), any(), any())).thenReturn(Mono.empty());
    }

    private void verifyScoreSaved(double expectedScore) {
        verify(scoreRepository).upsertScore(eq(COUNTY_ID), eq(VAR_ID), eq(YEAR), eq(RAW_VAL), eq(ADJ_VAL), eq(expectedScore));
    }
}
