package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.BdlDataRecordEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface BdlDataRecordRepository extends ReactiveCrudRepository<BdlDataRecordEntity, Long> {

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