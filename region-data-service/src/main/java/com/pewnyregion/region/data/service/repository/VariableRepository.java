
package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.VariableEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface VariableRepository extends ReactiveCrudRepository<VariableEntity, Integer> {

    Flux<VariableEntity> findByApiNameIn(List<String> apiNames);
}
