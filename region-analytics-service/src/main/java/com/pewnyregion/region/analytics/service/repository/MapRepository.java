package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.CountyVariableScoreEntity;
import com.pewnyregion.region.analytics.service.model.MapCountyScoreDto;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface MapRepository extends ReactiveCrudRepository<CountyVariableScoreEntity, Long> {

    @Query("""
        SELECT
            s.county_id AS county_id,
            c.name AS county_name,
            AVG(s.normalized_score) AS score
        FROM county_variable_scores s
        JOIN bdl_variables v ON v.id = s.bdl_variable_id
        JOIN counties c ON c.id = s.county_id
        WHERE v.api_name IN (:apiNames)
          AND s.year >= :yearFrom
          AND s.year <= :yearTo
          AND s.normalized_score IS NOT NULL
        GROUP BY s.county_id, c.name
    """)
    Flux<MapCountyScoreDto> getMapData(List<String> apiNames, Integer yearFrom, Integer yearTo);
}