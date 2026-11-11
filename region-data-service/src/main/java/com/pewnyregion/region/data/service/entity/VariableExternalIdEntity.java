package com.pewnyregion.region.data.service.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@Table("variable_external_ids")
public class VariableExternalIdEntity {

    @Id
    private Long id;

    @Column("variable_id")
    private Integer variableId;

    private String provider;

    @Column("external_id")
    private String externalId;
}