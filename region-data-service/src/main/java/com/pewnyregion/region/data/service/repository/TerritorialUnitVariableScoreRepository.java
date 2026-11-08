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

public interface TerritorialUnitVariableScoreRepository extends ReactiveCrudRepository<TerritorialUnitVariableScoreEntity, Long> {

    @Modifying
    @Query("""
        WITH latest_records AS MATERIALIZED (
            SELECT DISTINCT ON (
                unit.id,
                variable.id,
                data_record.year
            )
                unit.id AS territorial_unit_id,
                variable.id AS variable_id,
                variable.api_name,
                variable.direction,
                variable.per_capita,
                data_record.year,
                data_record.value AS raw_value
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
            ORDER BY
                unit.id,
                variable.id,
                data_record.year,
                data_record.imported_at DESC,
                data_record.id DESC
        ),
        adjusted_records AS (
            SELECT
                record.territorial_unit_id,
                record.variable_id,
                record.year,
                record.raw_value,
                record.direction,
                CASE
                    WHEN record.per_capita = TRUE
                        THEN record.raw_value / NULLIF(population.raw_value, 0)
                    ELSE record.raw_value
                END AS adjusted_value
            FROM latest_records record
            LEFT JOIN latest_records population
                ON population.territorial_unit_id = record.territorial_unit_id
                AND population.year = record.year
                AND population.api_name = 'population_in_thousands'
            WHERE record.per_capita = FALSE
               OR population.raw_value > 0
        ),
        percentile_records AS (
            SELECT
                territorial_unit_id,
                variable_id,
                year,
                raw_value,
                adjusted_value,
                direction,
                PERCENT_RANK() OVER (
                    PARTITION BY variable_id, year
                    ORDER BY adjusted_value
                ) AS percentile
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
            CASE
                WHEN direction = 'DESTIMULANT'
                    THEN 1.0 - percentile
                ELSE percentile
            END,
            :calculationJobId,
            CURRENT_TIMESTAMP
        FROM percentile_records
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
                SELECT job.id AS calculation_job_id
                FROM import_jobs job
                WHERE job.status = 'COMPLETED'
                  AND EXISTS (
                      SELECT 1
                      FROM territorial_unit_variable_scores scores
                      WHERE scores.calculation_job_id = job.id
                  )
                ORDER BY
                    COALESCE(job.finished_at, job.started_at) DESC NULLS LAST,
                    job.id DESC
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
    Flux<TerritorialUnitScoreWithAverage> findUnitDetails(@Param("territorialUnitId") Long territorialUnitId,
                                                          @Param("variableIds") List<Integer> variableIds);
}