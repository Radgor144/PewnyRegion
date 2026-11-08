package com.pewnyregion.region.data.service.model;

public record ImportJobCompletedEvent(
        String jobId,
        String provider
) {
}
