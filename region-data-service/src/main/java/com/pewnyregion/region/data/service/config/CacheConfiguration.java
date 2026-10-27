package com.pewnyregion.region.data.service.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.pewnyregion.region.data.service.model.CountyVariableCacheKey;
import com.pewnyregion.region.data.service.model.YearlyData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class CacheConfiguration {

    @Bean
    public Cache<CountyVariableCacheKey, List<YearlyData>> countyDetailsCache() {
        return Caffeine.newBuilder()
                       .maximumSize(20_000)
                       .expireAfterWrite(Duration.ofHours(6))
                       .recordStats()
                       .build();
    }
}