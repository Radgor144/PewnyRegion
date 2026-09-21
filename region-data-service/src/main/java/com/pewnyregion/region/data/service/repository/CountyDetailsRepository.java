package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.CountyVariableScoreEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface CountyDetailsRepository extends ReactiveCrudRepository<CountyVariableScoreEntity, Long> {

    @Query("""
                SELECT cvs.* 
                FROM county_variable_scores cvs
                JOIN counties c ON cvs.county_id = c.id
                WHERE c.teryt_code = :terytCode
                  AND cvs.bdl_variable_id IN (
                      SELECT bdl_variable_id 
                      FROM bdl_variable_ids 
                      WHERE bdl_id IN (:bdlVariableIds)
                  )
                ORDER BY cvs.year ASC
            """)
    Flux<CountyVariableScoreEntity> findByTerytCodeAndBdlIds(String terytCode, List<Integer> bdlVariableIds);
}
