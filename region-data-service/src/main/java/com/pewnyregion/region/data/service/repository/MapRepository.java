package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.TerritorialUnitVariableScoreEntity;
import com.pewnyregion.region.data.service.model.dto.MapCountyScoreDto;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface MapRepository extends ReactiveCrudRepository<TerritorialUnitVariableScoreEntity, Long> {

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
                unit.teryt_code AS teryt_code,
                unit.name AS county_name,
                ROUND(AVG(score.normalized_score)::numeric, 2)::double precision AS score
            FROM territorial_unit_variable_scores score
            JOIN latest_snapshot snapshot
                ON snapshot.calculation_job_id = score.calculation_job_id
            JOIN territorial_units unit
                ON unit.id = score.territorial_unit_id
            JOIN variables variable
                ON variable.id = score.variable_id
            WHERE variable.api_name IN (:apiNames)
              AND score.year >= :yearFrom
              AND score.year <= :yearTo
              AND unit.unit_level = 'COUNTY'
              AND unit.active = TRUE
            GROUP BY unit.teryt_code, unit.name
            ORDER BY unit.teryt_code
            """)
    Flux<MapCountyScoreDto> getMapData(@Param("apiNames") List<String> apiNames,
                                       @Param("yearFrom") Integer yearFrom,
                                       @Param("yearTo") Integer yearTo
    );

    @Query("""
            SELECT api_name
            FROM variables
            WHERE api_name IN (:apiNames)
            """)
    Flux<String> findExistingApiNames(@Param("apiNames") List<String> apiNames);
}