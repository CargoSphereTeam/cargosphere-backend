package com.cargosphere.shipment.integration.container;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(
        ContainerClientProperties.class
)
public class ContainerClientConfig {

    @Bean
    public RestClient containerRestClient(
            RestClient.Builder builder,
            ContainerClientProperties properties
    ) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                Math.toIntExact(
                        properties
                                .getConnectTimeout()
                                .toMillis()
                )
        );

        requestFactory.setReadTimeout(
                Math.toIntExact(
                        properties
                                .getReadTimeout()
                                .toMillis()
                )
        );

        return builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
