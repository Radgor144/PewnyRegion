package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.StatisticalDataRecordEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface StatisticalDataRecordRepository
        extends ReactiveCrudRepository<StatisticalDataRecordEntity, Long> {

    @Modifying
    @Query("""
            INSERT INTO statistical_data_records (
                territorial_unit_external_id_id,
                variable_external_id_id,
                year,
                value,
                import_job_id,
                imported_at
            )
            VALUES (
                :territorialUnitExternalIdId,
                :variableExternalIdId,
                :year,
                :value,
                :importJobId,
                :importedAt
            )
            """)
    Mono<Integer> insertRecord(
            @Param("territorialUnitExternalIdId") Long territorialUnitExternalIdId,
            @Param("variableExternalIdId") Long variableExternalIdId,
            @Param("year") Integer year,
            @Param("value") Double value,
            @Param("importJobId") String importJobId,
            @Param("importedAt") LocalDateTime importedAt
    );
}