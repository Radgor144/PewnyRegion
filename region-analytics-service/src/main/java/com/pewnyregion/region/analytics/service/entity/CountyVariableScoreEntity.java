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
@Table("county_variable_scores")
public class CountyVariableScoreEntity {
    @Id
    Long id;
    String countyId;
    Integer bdlVariableId;
    Integer year;
    Double rawValue;
    Double adjustedValue;
    Double normalizedScore;
    LocalDateTime calculatedAt;
}
