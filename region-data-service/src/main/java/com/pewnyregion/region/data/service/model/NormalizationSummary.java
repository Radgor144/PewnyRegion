package com.pewnyregion.region.data.service.model;

public record NormalizationSummary(int savedScores) {

    public String toMessage() {
        return "Normalized scores: " + savedScores;
    }
}