package com.cargosphere.shipment;

import com.cargosphere.shipment.config.OpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiEndpointTest {

    @Test
    void openApiConfigurationShouldContainShipmentApiMetadata() {

        OpenApiConfig openApiConfig =
                new OpenApiConfig();

        OpenAPI openAPI =
                openApiConfig.shipmentServiceOpenApi();

        assertThat(openAPI)
                .isNotNull();

        assertThat(openAPI.getInfo())
                .isNotNull();

        assertThat(openAPI.getInfo().getTitle())
                .isEqualTo(
                        "CargoSphere Shipment Service API"
                );

        assertThat(openAPI.getInfo().getVersion())
                .isEqualTo("v1");

        assertThat(openAPI.getComponents())
                .isNotNull();

        assertThat(
                openAPI
                        .getComponents()
                        .getSecuritySchemes()
        )
                .containsKey("bearerAuth");
    }
}