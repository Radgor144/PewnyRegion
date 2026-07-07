package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.ImportJobEntity;
import com.pewnyregion.region.analytics.service.model.consts.ImportJobStatus;
import com.pewnyregion.region.analytics.service.model.consts.ImportJobType;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ImportJobRepository extends ReactiveCrudRepository<ImportJobEntity, String> {
    Mono<Long> countByJobTypeAndStatus(ImportJobType jobType, ImportJobStatus status);
}