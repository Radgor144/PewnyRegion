package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.TerritorialUnitEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface TerritorialUnitRepository extends ReactiveCrudRepository<TerritorialUnitEntity, Long> {

    Mono<TerritorialUnitEntity> findByTerytCodeAndUnitLevelAndActive(String terytCode, String unitLevel, Boolean active);

    Mono<Long> countByUnitLevelAndActive(String unitLevel, Boolean active);

    @Modifying
    @Query("""
            INSERT INTO territorial_units (
                teryt_code,
                name,
                additional_name,
                unit_level,
                rodz,
                source_state_at,
                active
            )
            VALUES (
                :terytCode,
                :name,
                :additionalName,
                :unitLevel,
                :rodz,
                :sourceStateAt,
                TRUE
            )
            ON CONFLICT (teryt_code)
            DO UPDATE SET
                name = EXCLUDED.name,
                additional_name = EXCLUDED.additional_name,
                unit_level = EXCLUDED.unit_level,
                rodz = EXCLUDED.rodz,
                source_state_at = EXCLUDED.source_state_at,
                active = TRUE
            """)
    Mono<Integer> upsert(@Param("terytCode") String terytCode,
                         @Param("name") String name,
                         @Param("additionalName") String additionalName,
                         @Param("unitLevel") String unitLevel,
                         @Param("rodz") String rodz,
                         @Param("sourceStateAt") LocalDate sourceStateAt
    );

    @Modifying
    @Query("""
            UPDATE territorial_units child
            SET parent_unit_id = parent.id
            FROM territorial_units parent
            WHERE
                (
                    child.unit_level = 'COUNTY'
                    AND parent.unit_level = 'VOIVODESHIP'
                    AND parent.teryt_code = LEFT(child.teryt_code, 2)
                )
                OR
                (
                    child.unit_level = 'MUNICIPALITY'
                    AND parent.unit_level = 'COUNTY'
                    AND parent.teryt_code = LEFT(child.teryt_code, 4)
                )
            """)
    Mono<Integer> updateParentReferences();
}