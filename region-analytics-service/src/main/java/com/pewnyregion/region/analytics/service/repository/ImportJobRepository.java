package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.ImportJobEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportJobRepository extends ReactiveCrudRepository<ImportJobEntity, String> {
}