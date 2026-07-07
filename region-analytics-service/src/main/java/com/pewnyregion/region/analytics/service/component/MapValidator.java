package com.pewnyregion.region.analytics.service.component;

import com.pewnyregion.region.analytics.service.exception.MapValidationException;
import com.pewnyregion.region.analytics.service.repository.MapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MapValidator {

    private final MapRepository mapRepository;

    public Mono<Void> validate(List<String> apiNames) {
        return mapRepository.findExistingApiNames(apiNames)
                            .collect(Collectors.toSet())
                            .map(existingNames -> findMissingApiNames(apiNames, existingNames))
                            .flatMap(this::validateMissingApiNames);
    }

    private Mono<Void> validateMissingApiNames(List<String> missingApiNames) {
        return missingApiNames.isEmpty() ? Mono.empty()
                                         : Mono.error(new MapValidationException("Invalid names: " + missingApiNames));
    }

    private List<String> findMissingApiNames(List<String> apiNames, Set<String> existingNames) {
        return apiNames.stream()
                       .filter(name -> !existingNames.contains(name))
                       .distinct()
                       .toList();
    }
}