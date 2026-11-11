
package com.pewnyregion.region.data.service.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("variables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VariableEntity {

    @Id
    private Integer id;

    @Column("api_name")
    private String apiName;

    private String category;

    private String direction;

    @Column("per_capita")
    private boolean perCapita;

    private String unit;
}
