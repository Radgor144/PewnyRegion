package com.pewnyregion.region.data.service.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Table("territorial_unit_variable_scores")
public class TerritorialUnitVariableScoreEntity {

    @Id
    private Long id;

    @Column("territorial_unit_id")
    private Long territorialUnitId;

    @Column("variable_id")
    private Integer variableId;

    private Integer year;

    @Column("raw_value")
    private Double rawValue;

    @Column("adjusted_value")
    private Double adjustedValue;

    @Column("normalized_score")
    private Double normalizedScore;

    @Column("calculation_job_id")
    private String calculationJobId;

    @Column("calculated_at")
    private LocalDateTime calculatedAt;
}