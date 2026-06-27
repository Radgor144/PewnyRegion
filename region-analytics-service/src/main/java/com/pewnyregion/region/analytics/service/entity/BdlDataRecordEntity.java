package com.pewnyregion.region.analytics.service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("bdl_data_records")
public class BdlDataRecordEntity {

    @Id
    private Long id;
    private String countyId;
    private Integer variableId;
    private Integer year;
    private double value;
    private LocalDateTime importedAt;
    private Double normalizedScore;
}