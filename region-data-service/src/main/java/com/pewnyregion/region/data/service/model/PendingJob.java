package com.pewnyregion.region.data.service.model;

import reactor.core.publisher.Mono;

public record PendingJob(String jobId, Mono<String> task) {
}
