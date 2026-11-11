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
@Table("territorial_unit_external_ids")
public class TerritorialUnitExternalIdEntity {

    @Id
    private Long id;

    @Column("territorial_unit_id")
    private Long territorialUnitId;

    private String provider;

    @Column("external_id")
    private String externalId;

    @Column("provider_level")
    private String providerLevel;
}