package com.cargosphere.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void shouldConfigureAuthApiMetadataAndJwtScheme() {
        OpenApiConfig configuration =
                new OpenApiConfig();

        OpenAPI openAPI =
                configuration.authServiceOpenApi();

        assertThat(openAPI.getInfo().getTitle())
                .isEqualTo(
                        "CargoSphere Auth Service API"
                );

        assertThat(openAPI.getInfo().getVersion())
                .isEqualTo("v1");

        assertThat(openAPI.getComponents())
                .isNotNull();

        assertThat(
                openAPI
                        .getComponents()
                        .getSecuritySchemes()
        ).containsKey(
                OpenApiConfig.SECURITY_SCHEME_NAME
        );

        SecurityScheme securityScheme =
                openAPI
                        .getComponents()
                        .getSecuritySchemes()
                        .get(
                                OpenApiConfig
                                        .SECURITY_SCHEME_NAME
                        );

        assertThat(securityScheme.getType())
                .isEqualTo(SecurityScheme.Type.HTTP);

        assertThat(securityScheme.getScheme())
                .isEqualTo("bearer");

        assertThat(securityScheme.getBearerFormat())
                .isEqualTo("JWT");
    }
}