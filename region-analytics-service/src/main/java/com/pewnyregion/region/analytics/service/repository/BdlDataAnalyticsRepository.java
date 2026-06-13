package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.BdlDataRecordEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface BdlDataAnalyticsRepository extends ReactiveCrudRepository<BdlDataRecordEntity, Long> {

    @Query("""
        SELECT DISTINCT ON (year) * FROM bdl_data_records
        WHERE county_id = :countyId 
          AND variable_id = :variableId
        ORDER BY year DESC, imported_at DESC
    """)
    Flux<BdlDataRecordEntity> findLatestData(String countyId, Integer variableId);
}