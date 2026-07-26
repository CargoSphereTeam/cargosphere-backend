package com.cargosphere.container.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AuditClientProperties.class)
public class AuditClientConfig {

    @Bean
    @Qualifier("auditRestClient")
    public RestClient auditRestClient(
            RestClient.Builder builder,
            AuditClientProperties properties
    ) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                properties.getConnectTimeout()
        );

        requestFactory.setReadTimeout(
                properties.getReadTimeout()
        );

        return builder
                .baseUrl(normalizeBaseUrl(properties.getBaseUrl()))
                .requestFactory(requestFactory)
                .build();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Audit service base URL must be configured"
            );
        }

        String normalized = baseUrl.trim();

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(
                    0,
                    normalized.length() - 1
            );
        }

        return normalized;
    }
}