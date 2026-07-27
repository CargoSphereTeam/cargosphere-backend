package com.cargosphere.gateway;

import com.cargosphere.gateway.filter.GatewayCorsWebFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = ApiGatewayApplication.class,
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "app.cors.allowed-origin="
                        + "http://localhost:5173"
        }
)
@AutoConfigureWebTestClient
class ApiGatewayCorsTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private GatewayCorsWebFilter
            gatewayCorsWebFilter;

    @Test
    void gatewayCorsFilterShouldBeRegistered() {
        assertThat(gatewayCorsWebFilter)
                .isNotNull();
    }

    @Test
    void frontendPreflightRequestShouldBeAllowed() {
        webTestClient
                .options()
                .uri("/api/payments")
                .header(
                        HttpHeaders.ORIGIN,
                        "http://localhost:5173"
                )
                .header(
                        HttpHeaders
                                .ACCESS_CONTROL_REQUEST_METHOD,
                        "POST"
                )
                .header(
                        HttpHeaders
                                .ACCESS_CONTROL_REQUEST_HEADERS,
                        HttpHeaders.AUTHORIZATION
                )
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .valueEquals(
                        HttpHeaders
                                .ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"
                )
                .expectHeader()
                .valueEquals(
                        HttpHeaders
                                .ACCESS_CONTROL_ALLOW_CREDENTIALS,
                        "true"
                )
                .expectHeader()
                .value(
                        HttpHeaders
                                .ACCESS_CONTROL_ALLOW_METHODS,
                        value -> assertThat(value)
                                .contains("POST")
                )
                .expectHeader()
                .value(
                        HttpHeaders
                                .ACCESS_CONTROL_ALLOW_HEADERS,
                        value -> assertThat(
                                value.toLowerCase()
                        ).contains("authorization")
                );
    }

    @Test
    void unknownOriginShouldBeRejected() {
        webTestClient
                .options()
                .uri("/api/payments")
                .header(
                        HttpHeaders.ORIGIN,
                        "http://malicious.example"
                )
                .header(
                        HttpHeaders
                                .ACCESS_CONTROL_REQUEST_METHOD,
                        "POST"
                )
                .header(
                        HttpHeaders
                                .ACCESS_CONTROL_REQUEST_HEADERS,
                        HttpHeaders.AUTHORIZATION
                )
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void unsupportedRequestedHeaderShouldBeRejected() {
        webTestClient
                .options()
                .uri("/api/payments")
                .header(
                        HttpHeaders.ORIGIN,
                        "http://localhost:5173"
                )
                .header(
                        HttpHeaders
                                .ACCESS_CONTROL_REQUEST_METHOD,
                        "POST"
                )
                .header(
                        HttpHeaders
                                .ACCESS_CONTROL_REQUEST_HEADERS,
                        "X-Forbidden-Header"
                )
                .exchange()
                .expectStatus()
                .isForbidden();
    }
}