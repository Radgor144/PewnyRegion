package com.pewnyregion.region.analytics.service.utils;

import com.pewnyregion.region.analytics.service.entity.BdlVariableEntity;
import com.pewnyregion.region.analytics.service.entity.BdlVariableIdEntity;
import com.pewnyregion.region.analytics.service.model.consts.VariableDirection;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class VariableTestFixture {

    public static List<BdlVariableEntity> createBdlVariables() {
        return List.of(
                new BdlVariableEntity(1, "crimes", VariableDirection.DESTIMULANT.name(), true),
                new BdlVariableEntity(2, "population_in_thousands", VariableDirection.STIMULANT.name(), false),
                new BdlVariableEntity(3, "population_density", VariableDirection.STIMULANT.name(), false),
                new BdlVariableEntity(4, "unemployment", VariableDirection.DESTIMULANT.name(), false),
                new BdlVariableEntity(5, "gross_salary", VariableDirection.STIMULANT.name(), false)
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
