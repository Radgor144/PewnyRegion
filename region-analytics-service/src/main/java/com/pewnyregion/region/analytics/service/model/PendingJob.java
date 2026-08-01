package com.pewnyregion.region.analytics.service.model;

import reactor.core.publisher.Mono;

public record PendingJob(String jobId, Mono<String> task) {
}
