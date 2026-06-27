package com.pewnyregion.region.analytics.service.entity;

import com.pewnyregion.region.analytics.service.model.VariableDirection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("bdl_variables")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BdlVariableEntity {
    @Id
    private Integer id;
    private String apiName;
    private VariableDirection direction;
}