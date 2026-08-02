package com.cargosphere.shipment.integration.payment;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(
        PaymentClientProperties.class
)
public class PaymentClientConfig {

    @Bean
    public RestClient paymentRestClient(
            RestClient.Builder builder,
            PaymentClientProperties properties
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
