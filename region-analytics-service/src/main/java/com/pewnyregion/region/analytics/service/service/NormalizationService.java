package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.model.RawDataStatsDto;
import com.pewnyregion.region.analytics.service.repository.BdlDataRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NormalizationService {

    private final BdlDataRecordRepository dataRepository;

    public Mono<Void> calculateAndSaveScoresForAllYears() {
        log.info("Starting normalization for all years");

        return dataRepository.findDistinctYears()
                             .doOnNext(year -> log.info("Processing year: {}", year))
                             .concatMap(this::calculateAndSaveScoresForYear)
                             .then()
                             .doOnSuccess(v -> log.info("Normalization finished for all years"));
    }

    public Mono<Void> calculateAndSaveScoresForYears(List<Integer> years) {
        log.info("Starting normalization for selected years: {}", years);

        return Flux.fromIterable(years)
                   .distinct()
                   .doOnNext(year -> log.info("Processing year: {}", year))
                   .concatMap(this::calculateAndSaveScoresForYear)
                   .then()
                   .doOnSuccess(v -> log.info("Normalization finished for selected years"));
    }

    public Mono<Void> calculateAndSaveScoresForYear(Integer year) {
        log.info("Calculating stats for year: {}", year);

        return dataRepository.getStatsForYear(year)
                             .flatMap(this::processAndSaveStat)
                             .then()
                             .doOnSuccess(v -> log.info("Year {} normalized", year));
    }

    private Mono<Void> processAndSaveStat(RawDataStatsDto stat) {

        double z = calculateZScore(stat.value(), stat.meanVal(), stat.stddevVal());
        double score = scaleTo0To100(z, stat.direction());

        log.debug("County {}, var {}, year {} => score {}",
                stat.countyId(),
                stat.variableId(),
                stat.year(),
                score
        );

        return dataRepository.updateNormalizedScore(
                stat.countyId(),
                stat.variableId(),
                stat.year(),
                score
        );
    }

    private double calculateZScore(Double value, Double mean, Double stddev) {
        if (value == null || mean == null || stddev == null || stddev == 0.0) {
            return 0.0;
        }
        return (value - mean) / stddev;
    }

    private double scaleTo0To100(double zScore, String directionStr) {
        double clamped = Math.max(-3.0, Math.min(3.0, zScore));
        double score = ((clamped + 3.0) / 6.0) * 100.0;

        if ("DESTIMULANT".equals(directionStr)) {
            score = 100.0 - score;
        }

        return Math.round(score * 100.0) / 100.0;
    }
}