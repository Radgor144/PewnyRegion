package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.model.NormalizationSummary;
import com.pewnyregion.region.data.service.repository.TerritorialUnitVariableScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NormalizationServiceTest {

    private static final String JOB_ID = "normalization-job";
    private static final String PROVIDER = "BDL";

    private NormalizationService normalizationService;

    @Mock
    private TerritorialUnitVariableScoreRepository scoreRepository;

    @BeforeEach
    void setUp() {
        normalizationService = new NormalizationService(scoreRepository);
    }

    @Test
    void calculateAndSaveScores_returnsSummaryForPersistedScores() {
        when(scoreRepository.calculateAndSaveScores(JOB_ID, PROVIDER, true)).thenReturn(Mono.just(12));

        StepVerifier.create(normalizationService.calculateAndSaveScores(JOB_ID, PROVIDER, true))
                    .assertNext(summary -> assertThat(summary).isEqualTo(new NormalizationSummary(12)))
                    .verifyComplete();

        verify(scoreRepository).calculateAndSaveScores(JOB_ID, PROVIDER, true);
    }

    @Test
    void calculateAndSaveScores_failsWhenNoScoresAreProduced() {
        when(scoreRepository.calculateAndSaveScores(JOB_ID, PROVIDER, false)).thenReturn(Mono.just(0));

        StepVerifier.create(normalizationService.calculateAndSaveScores(JOB_ID, PROVIDER, false))
                    .expectErrorMessage("Normalization produced no scores")
                    .verify();
    }

    @Test
    void calculateAndSaveScores_requiresJobId() {
        StepVerifier.create(normalizationService.calculateAndSaveScores(" ", PROVIDER, false))
                    .expectErrorMessage("Calculation job ID is required")
                    .verify();

        verifyNoInteractions(scoreRepository);
    }

    @Test
    void calculateAndSaveScores_requiresProvider() {
        StepVerifier.create(normalizationService.calculateAndSaveScores(JOB_ID, null, false))
                    .expectErrorMessage("Provider is required")
                    .verify();

        verifyNoInteractions(scoreRepository);
    }
}
