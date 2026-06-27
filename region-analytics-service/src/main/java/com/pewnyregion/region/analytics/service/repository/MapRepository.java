package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.BdlDataRecordEntity;
import com.pewnyregion.region.analytics.service.model.MapCountyScoreDto;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface MapRepository extends ReactiveCrudRepository<BdlDataRecordEntity, Long> {

    @Query("""
        SELECT 
            r.county_id AS countyId,
            c.name AS countyName,
            AVG(r.normalized_score) AS score
        FROM bdl_data_records r
        JOIN bdl_variable_ids bvi ON r.variable_id = bvi.bdl_id
        JOIN bdl_variables v ON bvi.bdl_variable_id = v.id
        JOIN counties c ON c.id = r.county_id
        WHERE v.api_name IN (:apiNames)
          AND r.year >= :yearFrom
          AND r.year <= :yearTo
          AND r.normalized_score IS NOT NULL
        GROUP BY r.county_id, c.name
    """)
    Flux<MapCountyScoreDto> getMapData(List<String> apiNames, Integer yearFrom, Integer yearTo);
}