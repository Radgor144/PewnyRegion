package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.TerritorialUnitVariableScoreEntity;
import com.pewnyregion.region.data.service.model.TerritorialUnitScoreWithAverage;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TerritorialUnitVariableScoreRepository
        extends ReactiveCrudRepository<TerritorialUnitVariableScoreEntity, Long> {

    @Modifying
    @Query("""
            WITH eligible_records AS MATERIALIZED (
                SELECT
                    unit.id AS territorial_unit_id,
                    variable.id AS variable_id,
                    variable.api_name,
                    variable.direction,
                    variable.per_capita,
                    data_record.id AS record_id,
                    data_record.year,
                    data_record.value AS raw_value,
                    data_record.imported_at
                FROM statistical_data_records data_record
                JOIN territorial_unit_external_ids unit_external
                    ON unit_external.id = data_record.territorial_unit_external_id_id
                JOIN territorial_units unit
                    ON unit.id = unit_external.territorial_unit_id
                JOIN variable_external_ids variable_external
                    ON variable_external.id = data_record.variable_external_id_id
                JOIN variables variable
                    ON variable.id = variable_external.variable_id
                JOIN import_jobs source_job
                    ON source_job.id = data_record.import_job_id
                WHERE unit.unit_level = 'COUNTY'
                  AND unit.active = TRUE
                  AND unit_external.provider = :provider
                  AND variable_external.provider = :provider
                  AND source_job.provider = :provider
                  AND (
                      source_job.status = 'COMPLETED'
                      OR (
                          :includeCurrentJob = TRUE
                          AND source_job.id = :calculationJobId
                      )
                  )
            ),
            latest_records AS MATERIALIZED (
                SELECT DISTINCT ON (
                    territorial_unit_id,
                    variable_id,
                    year
                )
                    territorial_unit_id,
                    variable_id,
                    api_name,
                    direction,
                    per_capita,
                    year,
                    raw_value
                FROM eligible_records
                ORDER BY
                    territorial_unit_id,
                    variable_id,
                    year,
                    imported_at DESC,
                    record_id DESC
            ),
            population_values AS MATERIALIZED (
                SELECT
                    territorial_unit_id,
                    year,
                    raw_value AS population_value
                FROM latest_records
                WHERE api_name = 'population_in_thousands'
            ),
            adjusted_records AS MATERIALIZED (
                SELECT
                    record.territorial_unit_id,
                    record.variable_id,
                    record.year,
                    record.raw_value,
                    record.direction,
                    CASE
                        WHEN record.per_capita = TRUE
                            THEN record.raw_value / NULLIF(population.population_value, 0)
                        ELSE record.raw_value
                    END AS adjusted_value
                FROM latest_records record
                LEFT JOIN population_values population
                    ON population.territorial_unit_id = record.territorial_unit_id
                    AND population.year = record.year
                WHERE record.per_capita = FALSE
                   OR population.population_value > 0
            ),
            normalized_records AS (
                SELECT
                    territorial_unit_id,
                    variable_id,
                    year,
                    raw_value,
                    adjusted_value,
                    CASE
                        WHEN direction = 'DESTIMULANT'
                            THEN 1.0 - PERCENT_RANK() OVER (
                                PARTITION BY variable_id, year
                                ORDER BY adjusted_value
                            )
                        ELSE PERCENT_RANK() OVER (
                            PARTITION BY variable_id, year
                            ORDER BY adjusted_value
                        )
                    END AS normalized_score
                FROM adjusted_records
            )
            INSERT INTO territorial_unit_variable_scores (
                territorial_unit_id,
                variable_id,
                year,
                raw_value,
                adjusted_value,
                normalized_score,
                calculation_job_id,
                calculated_at
            )
            SELECT
                territorial_unit_id,
                variable_id,
                year,
                raw_value,
                adjusted_value,
                normalized_score,
                :calculationJobId,
                CURRENT_TIMESTAMP
            FROM normalized_records
            ON CONFLICT (
                territorial_unit_id,
                variable_id,
                year,
                calculation_job_id
            )
            DO UPDATE SET
                raw_value = EXCLUDED.raw_value,
                adjusted_value = EXCLUDED.adjusted_value,
                normalized_score = EXCLUDED.normalized_score,
                calculated_at = EXCLUDED.calculated_at
            """)
    Mono<Integer> calculateAndSaveScores(
            @Param("calculationJobId") String calculationJobId,
            @Param("provider") String provider,
            @Param("includeCurrentJob") boolean includeCurrentJob
    );

    @Query("""
            WITH latest_snapshot AS (
                SELECT scores.calculation_job_id
                FROM territorial_unit_variable_scores scores
                JOIN import_jobs job
                    ON job.id = scores.calculation_job_id
                WHERE job.status = 'COMPLETED'
                GROUP BY
                    scores.calculation_job_id,
                    job.finished_at,
                    job.started_at
                ORDER BY
                    COALESCE(job.finished_at, job.started_at) DESC,
                    scores.calculation_job_id DESC
                LIMIT 1
            )
            SELECT
                score.variable_id AS variable_id,
                score.year AS year,
                score.raw_value AS raw_value,
                ROUND(
                    AVG(score.raw_value) OVER (
                        PARTITION BY score.variable_id, score.year
                    )::numeric,
                    2
                )::double precision AS average_score
            FROM territorial_unit_variable_scores score
            JOIN latest_snapshot snapshot
                ON snapshot.calculation_job_id = score.calculation_job_id
            WHERE score.territorial_unit_id = :territorialUnitId
              AND score.variable_id IN (:variableIds)
            ORDER BY score.variable_id, score.year
            """)
    Flux<TerritorialUnitScoreWithAverage> findUnitDetails(
            @Param("territorialUnitId") Long territorialUnitId,
            @Param("variableIds") List<Integer> variableIds
    );
}