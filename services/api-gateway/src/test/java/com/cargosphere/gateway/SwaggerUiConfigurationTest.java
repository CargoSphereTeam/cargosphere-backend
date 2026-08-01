package com.cargosphere.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        classes = ApiGatewayApplication.class,
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "app.cors.allowed-origin=http://localhost:5173"
        }
)
@AutoConfigureWebTestClient
class SwaggerUiConfigurationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void swaggerUiShouldBeAvailable() {
        webTestClient
                .get()
                .uri("/swagger-ui.html")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueMatches(
                        "Location",
                        ".*/swagger-ui/index.html.*"
                );
    }

    @Test
    void swaggerConfigurationShouldContainAllServices() {
        webTestClient
                .get()
                .uri("/v3/api-docs/swagger-config")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.urls")
                .isArray()
                .jsonPath("$.urls.length()")
                .isEqualTo(6)

                .jsonPath(
                        "$.urls[?(@.url == "
                                + "'/openapi/auth-service')]"
                )
                .exists()

                .jsonPath(
                        "$.urls[?(@.url == "
                                + "'/openapi/shipment-service')]"
                )
                .exists()

                .jsonPath(
                        "$.urls[?(@.url == "
                                + "'/openapi/container-service')]"
                )
                .exists()

                .jsonPath(
                        "$.urls[?(@.url == "
                                + "'/openapi/document-service')]"
                )
                .exists()

                .jsonPath(
                        "$.urls[?(@.url == "
                                + "'/openapi/payment-service')]"
                )
                .exists()

                .jsonPath(
                        "$.urls[?(@.url == "
                                + "'/openapi/audit-service')]"
                )
                .exists();
    }
}