package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.CountyVariableScoreEntity;
import com.pewnyregion.region.data.service.model.CountyScoreWithAverage;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface CountyVariableScoreRepository extends ReactiveCrudRepository<CountyVariableScoreEntity, Long> {

    @Query("""
        SELECT scores.bdl_variable_id,
               scores.year,
               scores.raw_value,
               scores.average_score
        FROM (
            SELECT cvs.county_id,
                   cvs.bdl_variable_id,
                   cvs.year,
                   cvs.raw_value,
                   ROUND(
                       AVG(cvs.raw_value) OVER (
                           PARTITION BY cvs.bdl_variable_id, cvs.year
                       )::numeric,
                       2
                   ) AS average_score
            FROM county_variable_scores cvs
            WHERE cvs.raw_value IS NOT NULL
              AND cvs.bdl_variable_id IN (
                  SELECT bdl_variable_id
                  FROM bdl_variable_ids
                  WHERE bdl_id IN (:bdlIds)
              )
        ) scores
        WHERE scores.county_id = :countyId
        ORDER BY scores.bdl_variable_id, scores.year
    """)
    Flux<CountyScoreWithAverage> findCountyDetailsByCountyIdAndBdlIds(
            String countyId,
            List<Integer> bdlIds
    );

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