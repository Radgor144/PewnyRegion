package com.pewnyregion.region.analytics.service.model;

import java.util.List;

public record NormalizationSummary(List<Integer> processedYears) {
    public String toMessage() {
        return "Normalized " + processedYears.size() + " years: " + processedYears;
    }
}