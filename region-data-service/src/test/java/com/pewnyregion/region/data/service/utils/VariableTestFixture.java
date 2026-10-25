package com.pewnyregion.region.data.service.utils;

import com.pewnyregion.region.data.service.entity.BdlVariableEntity;
import com.pewnyregion.region.data.service.entity.BdlVariableIdEntity;
import com.pewnyregion.region.data.service.model.consts.VariableDirection;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class VariableTestFixture {

    public static List<BdlVariableEntity> createBdlVariables() {
        return List.of(
                new BdlVariableEntity(1, "gross_salary", "FINANCE", VariableDirection.STIMULANT.name(), false, "PLN"),
                new BdlVariableEntity(7, "unemployment", "LABOR", VariableDirection.DESTIMULANT.name(), false, "%"),
                new BdlVariableEntity(8, "population_in_thousands", "POPULATION", VariableDirection.STIMULANT.name(), false, "k"),
                new BdlVariableEntity(9, "population_density", "POPULATION", VariableDirection.STIMULANT.name(), false, "os/km2"),
                new BdlVariableEntity(10, "crimes_total", "SAFETY", VariableDirection.DESTIMULANT.name(), true, "per_1000")
        );
    }

    public static List<BdlVariableIdEntity> createBdlVariablesIds() {
        return List.of(
                new BdlVariableIdEntity(1, 10, 58559),
                new BdlVariableIdEntity(2, 10, 1749155),
                new BdlVariableIdEntity(3, 8, 1645341),
                new BdlVariableIdEntity(4, 9, 60559),
                new BdlVariableIdEntity(5, 7, 60270),
                new BdlVariableIdEntity(6, 1, 64428)
        );
    }
}
