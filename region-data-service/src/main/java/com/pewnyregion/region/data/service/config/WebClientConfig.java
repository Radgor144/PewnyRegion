
package com.pewnyregion.region.data.service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${bdl.api.key}")
    private String apiKey;

    @Value("${bdl.api.url}")
    private String apiUrl;

    @Bean
    public WebClient bdlWebClient() {
        return WebClient.builder()
                        .baseUrl(apiUrl)
                        .defaultHeader("X-ClientId", apiKey)
                        .filter((request, next) -> {
                            log.info("BDL request: {} {}", request.method(), request.url());
                            return next.exchange(request);
                        })
                        .build();
    }
}
