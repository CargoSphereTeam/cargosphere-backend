package com.cargosphere.payment.audit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(
        AuditClientProperties.class
)
public class AuditClientConfig {

    @Bean("paymentAuditRestClient")
    public RestClient paymentAuditRestClient(
            RestClient.Builder builder,
            AuditClientProperties properties
    ) {
        SimpleClientHttpRequestFactory
                requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                properties.getConnectTimeout()
        );

        requestFactory.setReadTimeout(
                properties.getReadTimeout()
        );

        return builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}