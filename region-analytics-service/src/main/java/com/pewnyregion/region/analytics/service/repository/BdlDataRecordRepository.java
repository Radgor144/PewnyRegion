package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.BdlDataRecordEntity;
import com.pewnyregion.region.analytics.service.model.RawDataStatsDto;
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
        SELECT
            county_id,
            variable_id,
            year,
            AVG(value) AS value,
            AVG(value) AS mean_val,
            STDDEV(value) AS stddev_val,
            v.direction AS direction
        FROM bdl_data_records r
        JOIN bdl_variable_ids bvi ON r.variable_id = bvi.bdl_id
        JOIN bdl_variables v ON bvi.bdl_variable_id = v.id
        WHERE r.year = :year
        GROUP BY county_id, variable_id, year, v.direction
    """)
    Flux<RawDataStatsDto> getStatsForYear(Integer year);

    @Query("""
        UPDATE bdl_data_records
        SET normalized_score = :score
        WHERE county_id = :countyId
        AND variable_id = :variableId
        AND year = :year
    """)
    Mono<Void> updateNormalizedScore(String countyId, Integer variableId, Integer year, Double score);

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