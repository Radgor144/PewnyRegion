package com.pewnyregion.region.analytics.service.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum BdlVarId {
    CRIMES(
            "crimes",
            List.of(58559, 1749155)    // 2013 - 2024 and 2025 - recent
    ),
    POPULATION_IN_THOUSANDS(
            "population_in_thousands",
            List.of(1645341)
    ),
    POPULATION_DENSITY(
            "population_density",
            List.of(60559)
    ),
    Unemployment(
            "unemployment",
            List.of(60270)
    ),
    GROSS_SALARY(
            "gross_salary",
            List.of(64428)
    );

    private final String apiName;
    private final List<Integer> varIds;

    public static List<String> getAllBdlVarIds() {
        return Arrays.stream(BdlVarId.values())
                .map(BdlVarId::getApiName)
                .toList();
    }

    public static List<BdlVarId> fromApiNames(List<String> values) {
        return values.stream()
                .map(BdlVarId::fromApiName)
                .toList();
    }

    public static BdlVarId fromApiName(String value) {
        return Arrays.stream(values())
                .filter(v -> v.apiName.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow();
    }

    public static BdlVarId fromVarId(int varId) {
        return Arrays.stream(values())
                .filter(v -> v.varIds.contains(varId))
                .findFirst()
                .orElseThrow();
    }
}

