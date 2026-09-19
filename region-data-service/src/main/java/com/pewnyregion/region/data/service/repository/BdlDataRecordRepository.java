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
    WITH resolved AS (
        SELECT
            r.county_id,
            bvi.bdl_variable_id,
            r.year,
            r.value AS raw_value,
            v.per_capita,
            v.direction
        FROM bdl_data_records r
        JOIN bdl_variable_ids bvi ON r.variable_id = bvi.bdl_id
        JOIN bdl_variables v ON bvi.bdl_variable_id = v.id
        WHERE r.year = :year
    ),
    population AS (
        SELECT r.county_id, r.year, r.value AS population
        FROM bdl_data_records r
        JOIN bdl_variable_ids bvi ON r.variable_id = bvi.bdl_id
        JOIN bdl_variables v ON bvi.bdl_variable_id = v.id
        WHERE v.api_name = 'population_in_thousands' AND r.year = :year
    ),
    adjusted AS (
        SELECT
            res.county_id,
            res.bdl_variable_id,
            res.year,
            res.raw_value,
            CASE WHEN res.per_capita THEN res.raw_value / p.population ELSE res.raw_value END AS adjusted_value,
            res.direction
        FROM resolved res
        LEFT JOIN population p ON p.county_id = res.county_id AND p.year = res.year
    )
    SELECT 
        a.county_id, 
        a.bdl_variable_id, 
        a.year, 
        a.raw_value, 
        a.adjusted_value,
        a.direction,
        PERCENT_RANK() OVER (PARTITION BY a.bdl_variable_id ORDER BY a.adjusted_value) AS percentile
    FROM adjusted a
    WHERE a.adjusted_value IS NOT NULL
""")
    Flux<NormalizationStatsDto> getNormalizationStatsForYear(Integer year);

    @Modifying
    @Query("""
        INSERT INTO bdl_data_records (county_id, variable_id, year, value, imported_at)
        VALUES (:countyId, :variableId, :year, :value, :importedAt)
        ON CONFLICT (county_id, variable_id, year) 
        DO UPDATE SET 
            value = EXCLUDED.value,
            imported_at = EXCLUDED.imported_at
    """)
    Mono<Void> upsertRecord(String countyId, Integer variableId, Integer year, Double value, LocalDateTime importedAt);
}