
package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.entity.VariableEntity;
import com.pewnyregion.region.data.service.model.VariableResponse;
import com.pewnyregion.region.data.service.model.consts.VariableDirection;
import com.pewnyregion.region.data.service.repository.VariableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class VariableService {

    private final VariableRepository variableRepository;

    public Flux<VariableResponse> getAllVariables() {
        return variableRepository.findAll()
                                 .map(this::mapToResponse);
    }

    private VariableResponse mapToResponse(VariableEntity variable) {
        return new VariableResponse(
                variable.getApiName(),
                variable.getCategory(),
                VariableDirection.valueOf(variable.getDirection()),
                variable.isPerCapita(),
                variable.getUnit()
        );
    }
}
