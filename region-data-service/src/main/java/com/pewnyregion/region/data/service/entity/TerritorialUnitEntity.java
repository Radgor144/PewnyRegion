package com.pewnyregion.region.data.service.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Table("territorial_units")
public class TerritorialUnitEntity {

        @Id
        private Long id;

        @Column("teryt_code")
        private String terytCode;

        private String name;

        @Column("additional_name")
        private String additionalName;

        @Column("unit_level")
        private String unitLevel;

        private String rodz;

        @Column("parent_unit_id")
        private Long parentUnitId;

        @Column("source_state_at")
        private LocalDate sourceStateAt;

        private Boolean active;
}