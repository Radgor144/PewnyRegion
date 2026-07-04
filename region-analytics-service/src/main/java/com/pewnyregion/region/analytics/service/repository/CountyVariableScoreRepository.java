package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.CountyVariableScoreEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CountyVariableScoreRepository extends ReactiveCrudRepository<CountyVariableScoreEntity, Long> {

    @Modifying
    @Query("""
        INSERT INTO county_variable_scores
            (county_id, bdl_variable_id, year, raw_value, adjusted_value, normalized_score, calculated_at)
        VALUES (:countyId, :bdlVariableId, :year, :rawValue, :adjustedValue, :normalizedScore, now())
        ON CONFLICT (county_id, bdl_variable_id, year)
        DO UPDATE SET
            raw_value = EXCLUDED.raw_value,
            adjusted_value = EXCLUDED.adjusted_value,
            normalized_score = EXCLUDED.normalized_score,
            calculated_at = EXCLUDED.calculated_at
    """)
    Mono<Void> upsertScore(String countyId, Integer bdlVariableId, Integer year,
                           Double rawValue, Double adjustedValue, Double normalizedScore);
}