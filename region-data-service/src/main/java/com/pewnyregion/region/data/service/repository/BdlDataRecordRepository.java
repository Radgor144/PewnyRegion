package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.BdlDataRecordEntity;
import com.pewnyregion.region.data.service.model.dto.NormalizationStatsDto;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface BdlDataRecordRepository extends ReactiveCrudRepository<BdlDataRecordEntity, Long> {

    @Query("""
        SELECT DISTINCT year
        FROM bdl_data_records
    """)
    Flux<Integer> findDistinctYears();

    @Query("""
    WITH population AS (
        SELECT
            r.county_id,
            r.year,
            r.value AS population
        FROM bdl_data_records r
        JOIN bdl_variable_ids bvi ON bvi.bdl_id = r.variable_id
        JOIN bdl_variables v ON v.id = bvi.bdl_variable_id
        WHERE v.api_name = 'population_in_thousands'
          AND r.year = :year
    ),
    adjusted AS (
        SELECT
            r.county_id,
            bvi.bdl_variable_id,
            r.year,
            r.value AS raw_value,
            CASE
                WHEN v.per_capita THEN r.value / NULLIF(p.population, 0)
                ELSE r.value
            END AS adjusted_value,
            v.direction
        FROM bdl_data_records r
        JOIN bdl_variable_ids bvi ON bvi.bdl_id = r.variable_id
        JOIN bdl_variables v ON v.id = bvi.bdl_variable_id
        LEFT JOIN population p
            ON p.county_id = r.county_id
           AND p.year = r.year
        WHERE r.year = :year
    )
    SELECT
        a.county_id,
        a.bdl_variable_id,
        a.year,
        a.raw_value,
        a.adjusted_value,
        a.direction,
        PERCENT_RANK() OVER (
            PARTITION BY a.bdl_variable_id
            ORDER BY a.adjusted_value
        ) AS percentile
    FROM adjusted a
    WHERE a.adjusted_value IS NOT NULL
    """)
    Flux<NormalizationStatsDto> getNormalizationStatsForYear(Integer year);

    @Modifying
    @Query("""
        INSERT INTO bdl_data_records (county_id, variable_id, year, value, imported_at)
        VALUES (:countyId, :variableId, :year, :value, :importedAt)
    """)
    Mono<Void> insertRecord(String countyId, Integer variableId, Integer year, Double value, LocalDateTime importedAt);
}