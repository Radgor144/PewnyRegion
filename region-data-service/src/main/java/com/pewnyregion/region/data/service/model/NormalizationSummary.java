package com.pewnyregion.region.data.service.model;

import java.util.List;

public record NormalizationSummary(List<Integer> processedYears) {

    public NormalizationSummary {
        processedYears = processedYears.stream().sorted().toList();
    }

    public String toMessage() {
        return "Normalized " + processedYears.size() + " years: " + processedYears;
    }
}