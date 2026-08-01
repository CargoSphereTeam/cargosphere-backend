package com.cargosphere.audit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    public static final String JWT_SCHEME_NAME = "bearerAuth";
    public static final String INTERNAL_API_KEY_SCHEME_NAME =
            "internalApiKey";

    @Bean
    public OpenAPI auditServiceOpenApi() {
        SecurityScheme jwtScheme =
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                                "JWT access token returned by auth-service."
                        );

        SecurityScheme internalApiKeyScheme =
                new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Internal-API-Key")
                        .description(
                                "Internal API key used by CargoSphere services."
                        );

        return new OpenAPI()
                .info(
                        new Info()
                                .title("CargoSphere Audit Service API")
                                .version("v1")
                                .description(
                                        "Audit-log creation, lookup and "
                                                + "filtering APIs."
                                )
                                .contact(
                                        new Contact()
                                                .name("CargoSphere Team")
                                )
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        JWT_SCHEME_NAME,
                                        jwtScheme
                                )
                                .addSecuritySchemes(
                                        INTERNAL_API_KEY_SCHEME_NAME,
                                        internalApiKeyScheme
                                )
                );
    }
}
