package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.ImportJobEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ImportJobRepository extends ReactiveCrudRepository<ImportJobEntity, String> {}