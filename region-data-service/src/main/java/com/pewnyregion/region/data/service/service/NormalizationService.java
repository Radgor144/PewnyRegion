package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.model.NormalizationSummary;
import com.pewnyregion.region.data.service.repository.TerritorialUnitVariableScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class NormalizationService {

    private final TerritorialUnitVariableScoreRepository scoreRepository;

    public Mono<NormalizationSummary> calculateAndSaveScores(String calculationJobId,
                                                             String provider,
                                                             boolean includeCurrentJob) {
        if (calculationJobId == null || calculationJobId.isBlank()) {
            return Mono.error(new IllegalArgumentException("Calculation job ID is required"));
        }

        if (provider == null || provider.isBlank()) {
            return Mono.error(new IllegalArgumentException("Provider is required"));
        }

        log.info("Starting normalization: jobId={}, provider={}, includeCurrentJob={}", calculationJobId, provider, includeCurrentJob);

        return scoreRepository.calculateAndSaveScores(calculationJobId, provider, includeCurrentJob)
                              .flatMap(savedScores -> {
                                  if (savedScores == null || savedScores == 0) {
                                      return Mono.error(new IllegalStateException("Normalization produced no scores"));
                                  }
                                  log.info("Normalization completed: jobId={}, savedScores={}", calculationJobId, savedScores);
                                  return Mono.just(new NormalizationSummary(savedScores));
                              });
    }
}