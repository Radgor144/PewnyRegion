package com.pewnyregion.region.data.service.entity;

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
@Table("bdl_raw_import_values")
public class BdlDataRecordEntity {

    @Id
    private Long id;
    private String countyId;
    private Integer variableId;
    private Integer year;
    private Double value;
    private LocalDateTime importedAt;
}