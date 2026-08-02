package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.model.NormalizationSummary;
import com.pewnyregion.region.data.service.model.consts.VariableDirection;
import com.pewnyregion.region.data.service.model.dto.NormalizationStatsDto;
import com.pewnyregion.region.data.service.repository.BdlDataRecordRepository;
import com.pewnyregion.region.data.service.repository.CountyVariableScoreRepository;
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

    private static final double Z_SCORE_CLAMP = 3.0;
    private static final double SCORE_SCALE_MAX = 100.0;

    private final BdlDataRecordRepository dataRepository;
    private final CountyVariableScoreRepository scoreRepository;

    public Mono<NormalizationSummary> calculateAndSaveScoresForAllYears() {
        return calculateAndSaveScores(dataRepository.findDistinctYears());
    }

    public Mono<NormalizationSummary> calculateAndSaveScoresForYears(List<Integer> years) {
        return calculateAndSaveScores(Flux.fromIterable(years));
    }

    private Mono<NormalizationSummary> calculateAndSaveScores(Flux<Integer> years) {
        log.info("[NORM] START");
        return years.distinct()
                    .concatMap(this::processYear)
                    .collectList()
                    .map(NormalizationSummary::new)
                    .doOnSuccess(s -> log.info("[NORM] DONE — {}", s.toMessage()));
    }

    private Mono<Integer> processYear(Integer year) {
        log.info("[NORM] Processing year: {}", year);
        return dataRepository.getNormalizationStatsForYear(year)
                             .flatMap(this::processStat)
                             .then(Mono.just(year))
                             .doOnSuccess(y -> log.info("[NORM] Year {} done", y));
    }

    private Mono<Void> processStat(NormalizationStatsDto stat) {
        return calculateScore(stat)
                .flatMap(score -> saveScore(stat, score));
    }

    private Mono<Double> calculateScore(NormalizationStatsDto stat) {
        if (stat.adjustedValue() == null) {
            log.warn("[NORM] Missing population data for county={}, variable={}, year={} — skipping",
                    stat.countyId(), stat.bdlVariableId(), stat.year());
            return Mono.empty();
        }

        double z = calculateZ(stat.adjustedValue(), stat.meanVal(), stat.stddevVal());
        if (Double.isNaN(z)) {
            log.warn("[NORM] Could not compute z-score for county={}, variable={}, year={} — skipping",
                    stat.countyId(), stat.bdlVariableId(), stat.year());
            return Mono.empty();
        }

        return Mono.just(scale(z, stat.direction()));
    }

    private Mono<Void> saveScore(NormalizationStatsDto stat, double score) {
        return scoreRepository.upsertScore(stat.countyId(), stat.bdlVariableId(), stat.year(),
                                           stat.rawValue(), stat.adjustedValue(), score
        ).then();
    }

    private double calculateZ(Double value, Double mean, Double stddev) {
        if (value == null || mean == null || stddev == null) return Double.NaN;
        if (stddev == 0) return 0.0;
        return (value - mean) / stddev;
    }

    private double scale(double z, String direction) {
        double clamped = Math.max(-Z_SCORE_CLAMP, Math.min(Z_SCORE_CLAMP, z));
        double score = ((clamped + Z_SCORE_CLAMP) / (Z_SCORE_CLAMP * 2)) * SCORE_SCALE_MAX;
        if (VariableDirection.valueOf(direction) == VariableDirection.DESTIMULANT) {
            score = SCORE_SCALE_MAX - score;
        }
        return roundToTwoDecimals(score);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
