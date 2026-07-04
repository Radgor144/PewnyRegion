package com.pewnyregion.region.analytics.service.model;

public record ImportSummary(int processedChunks) {
    public String toMessage() {
        return "Processed " + processedChunks + " data chunks";
    }
}