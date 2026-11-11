package com.pewnyregion.region.data.service.utils;

import com.pewnyregion.region.data.service.entity.VariableEntity;
import com.pewnyregion.region.data.service.model.consts.VariableDirection;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class VariableTestFixture {

    public static List<VariableEntity> createVariables() {
        return List.of(
                new VariableEntity(1, "gross_salary", "FINANCE", VariableDirection.STIMULANT.name(), false, "PLN"),
                new VariableEntity(7, "unemployment", "LABOR", VariableDirection.DESTIMULANT.name(), false, "%"),
                new VariableEntity(8, "population_in_thousands", "POPULATION", VariableDirection.STIMULANT.name(), false, "k"),
                new VariableEntity(9, "population_density", "POPULATION", VariableDirection.STIMULANT.name(), false, "os/km2"),
                new VariableEntity(10, "crimes_total", "SAFETY", VariableDirection.DESTIMULANT.name(), true, "per_1000")
        );
    }
}
