package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.model.dto.NormalizationStatsDto;
import com.pewnyregion.region.analytics.service.model.NormalizationSummary;
import com.pewnyregion.region.analytics.service.repository.BdlDataRecordRepository;
import com.pewnyregion.region.analytics.service.repository.CountyVariableScoreRepository;
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
    private final CountyVariableScoreRepository scoreRepository;

    public Mono<NormalizationSummary> calculateAndSaveScoresForAllYears() {
        log.info("[NORM] START");
        return dataRepository.findDistinctYears()
                             .distinct()
                             .concatMap(this::processYear)
                             .collectList()
                             .map(NormalizationSummary::new)
                             .doOnSuccess(s -> log.info("[NORM] DONE — {}", s.toMessage()));
    }

    public Mono<NormalizationSummary> calculateAndSaveScoresForYears(List<Integer> years) {
        return Flux.fromIterable(years)
                   .distinct()
                   .concatMap(this::processYear)
                   .collectList()
                   .map(NormalizationSummary::new)
                   .doOnSuccess(s -> log.info("[NORM] DONE — {}", s.toMessage()));
    }

    private Mono<Integer> processYear(Integer year) {
        log.info("[NORM] Processing year: {}", year);
        return dataRepository.getNormalizationStatsForYear(year)
                             .flatMap(this::process)
                             .then(Mono.just(year))
                             .doOnSuccess(y -> log.info("[NORM] Year {} done", y));
    }

    private Mono<Void> process(NormalizationStatsDto stat) {
        if (stat.adjustedValue() == null) {
            log.warn("[NORM] Missing population data for county={}, variable={}, year={} — skipping",
                    stat.countyId(), stat.bdlVariableId(), stat.year());
            return Mono.empty();
        }

        double z = calculateZ(stat.adjustedValue(), stat.meanVal(), stat.stddevVal());
        if (Double.isNaN(z)) {
            return Mono.empty();
        }

        double score = scale(z, stat.direction());

        return scoreRepository.upsertScore(
                stat.countyId(), stat.bdlVariableId(), stat.year(),
                stat.rawValue(), stat.adjustedValue(), score
        ).then();
    }

    private double calculateZ(Double value, Double mean, Double stddev) {
        if (value == null || mean == null || stddev == null) return Double.NaN;
        if (stddev == 0) return 0.0;
        return (value - mean) / stddev;
    }

    private double scale(double z, String direction) {
        double clamped = Math.max(-3.0, Math.min(3.0, z));
        double score = ((clamped + 3.0) / 6.0) * 100.0;
        if ("DESTIMULANT".equals(direction)) {
            score = 100.0 - score;
        }
        return Math.round(score * 100.0) / 100.0;
    }
}