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
                new BdlVariableEntity(1, "crimes_total", "SAFETY", VariableDirection.DESTIMULANT.name(), true, "pcs"),
                new BdlVariableEntity(2, "population_in_thousands", "POPULATION", VariableDirection.STIMULANT.name(), false, "thousands"),
                new BdlVariableEntity(3, "population_density", "POPULATION", VariableDirection.STIMULANT.name(), false, "persons/km2"),
                new BdlVariableEntity(4, "unemployment", "LABOR", VariableDirection.DESTIMULANT.name(), false, "%"),
                new BdlVariableEntity(5, "gross_salary", "FINANCE", VariableDirection.STIMULANT.name(), false, "PLN")
        );
    }

    public static List<BdlVariableIdEntity> createBdlVariablesIds() {
        return List.of(
                new BdlVariableIdEntity(1, 1, 58559),
                new BdlVariableIdEntity(2, 1, 1749155),
                new BdlVariableIdEntity(3, 2, 1645341),
                new BdlVariableIdEntity(4, 3, 60559),
                new BdlVariableIdEntity(5, 4, 60270),
                new BdlVariableIdEntity(6, 5, 64428)
        );
    }
}
