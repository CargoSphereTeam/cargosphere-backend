package com.cargosphere.audit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void shouldConfigureApiMetadataAndSecuritySchemes() {
        OpenAPI openAPI =
                new OpenApiConfig().auditServiceOpenApi();

        assertThat(openAPI.getInfo().getTitle())
                .isEqualTo("CargoSphere Audit Service API");

        assertThat(openAPI.getInfo().getVersion())
                .isEqualTo("v1");

        assertThat(openAPI.getComponents().getSecuritySchemes())
                .containsKeys(
                        OpenApiConfig.JWT_SCHEME_NAME,
                        OpenApiConfig.INTERNAL_API_KEY_SCHEME_NAME
                );

        SecurityScheme jwtScheme =
                openAPI.getComponents()
                        .getSecuritySchemes()
                        .get(OpenApiConfig.JWT_SCHEME_NAME);

        assertThat(jwtScheme.getType())
                .isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(jwtScheme.getScheme())
                .isEqualTo("bearer");

        SecurityScheme apiKeyScheme =
                openAPI.getComponents()
                        .getSecuritySchemes()
                        .get(
                                OpenApiConfig
                                        .INTERNAL_API_KEY_SCHEME_NAME
                        );

        assertThat(apiKeyScheme.getType())
                .isEqualTo(SecurityScheme.Type.APIKEY);
        assertThat(apiKeyScheme.getName())
                .isEqualTo("X-Internal-API-Key");
    }
}
