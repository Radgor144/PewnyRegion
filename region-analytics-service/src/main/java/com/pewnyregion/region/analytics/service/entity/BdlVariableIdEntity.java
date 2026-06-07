package com.pewnyregion.region.analytics.service.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("bdl_variable_ids")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BdlVariableIdEntity {
    @Id
    private Integer id;
    private Integer bdlVariableId;
    private Integer bdlId;
}