package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.entity.CountyEntity;
import com.pewnyregion.region.analytics.service.repository.CountyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CountyReactiveProvider {

    private static final int PAGE_SIZE = 10;
    private final CountyRepository countyRepository;

    private record PageState(int page, List<CountyEntity> content) {}

    public Flux<CountyEntity> streamAllCounties() {
        return countyRepository.findAllBy(PageRequest.of(0, PAGE_SIZE))
                .collectList()
                .map(list -> new PageState(0, list))
                .expand(this::getNextPageOfCounties)
                .flatMapIterable(PageState::content);
    }

    private Mono<PageState> getNextPageOfCounties(PageState state) {
        return Mono.just(state)
                .filter(s -> s.content().size() == PAGE_SIZE)
                .flatMap(s -> countyRepository.findAllBy(PageRequest.of(s.page() + 1, PAGE_SIZE))
                        .collectList()
                        .map(newList -> new PageState(s.page() + 1, newList))
                );
    }
}