package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.client.BdlClient;
import com.pewnyregion.region.analytics.service.entity.CountyEntity;
import com.pewnyregion.region.analytics.service.model.BdlResponse;
import com.pewnyregion.region.analytics.service.model.BdlUnit;
import com.pewnyregion.region.analytics.service.repository.CountyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CountyImportService {

    public static final int LEVEL = 5;

    private final BdlClient bdlClient;
    private final CountyRepository countyRepository;

    public Mono<Void> importCounties() {
        return fetchPage(0)
                .expand(response -> {
                    if (Objects.isNull(response.links()) || Objects.isNull(response.links().getNext())) {
                        return Mono.empty();
                    }
                    int nextPage = response.page() + 1;
                    return fetchPage(nextPage);
                })
                .flatMapIterable(BdlResponse::results)
                .map(this::mapToEntity)
                .as(countyRepository::saveAll)
                .then();
    }

    private Mono<BdlResponse> fetchPage(int page) {
        return bdlClient.fetchPage(LEVEL, page);
    }

    private CountyEntity mapToEntity(BdlUnit unit) {
        return CountyEntity.builder()
                .id(unit.id())
                .name(unit.name())
                .parentId(unit.parentId())
                .level(unit.level())
                .terytCode(extractTerytCode(unit.id()))
                .isNew(true)
                .build();
    }

    private String extractTerytCode(String id) {
        return id.substring(2, 4) + id.substring(7, 9);
    }
}