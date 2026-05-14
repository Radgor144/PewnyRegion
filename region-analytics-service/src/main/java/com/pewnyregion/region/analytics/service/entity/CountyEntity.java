package com.pewnyregion.region.analytics.service.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;


@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table("counties")
public class CountyEntity implements Persistable<String> {

    @Id
    private String id;
    private String name;
    private String parentId;
    private Integer level;
    private String teryt;

    @Transient
    private boolean isNew;

    @Override
    public boolean isNew() {
        return this.isNew || id == null;
    }
}