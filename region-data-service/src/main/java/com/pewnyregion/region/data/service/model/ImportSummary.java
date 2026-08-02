package com.pewnyregion.region.data.service.model;

public record ImportSummary(int processedChunks) {
    public String toMessage() {
        return "Processed " + processedChunks + " data chunks";
    }
}