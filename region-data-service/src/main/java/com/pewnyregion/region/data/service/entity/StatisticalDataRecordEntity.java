package com.pewnyregion.region.data.service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("statistical_data_records")
public class StatisticalDataRecordEntity {

    @Id
    private Long id;

    @Column("territorial_unit_external_id_id")
    private Long territorialUnitExternalIdId;

    @Column("variable_external_id_id")
    private Long variableExternalIdId;

    private Integer year;

    private Double value;

    @Column("import_job_id")
    private String importJobId;

    @Column("imported_at")
    private LocalDateTime importedAt;
}