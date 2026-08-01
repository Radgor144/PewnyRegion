package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.ImportJobEntity;
import com.pewnyregion.region.analytics.service.model.consts.ImportJobStatus;
import com.pewnyregion.region.analytics.service.model.consts.ImportJobType;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Repository
public interface ImportJobRepository extends ReactiveCrudRepository<ImportJobEntity, String> {
    Mono<Long> countByJobTypeAndStatusIn(ImportJobType type, Collection<ImportJobStatus> statuses);
}