package com.cargosphere.documentservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI documentServiceOpenApi() {
        SecurityScheme jwtSecurityScheme =
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                                "Enter the JWT access token returned "
                                        + "by the auth-service login API."
                        );

        return new OpenAPI()
                .info(
                        new Info()
                                .title("CargoSphere Document Service API")
                                .version("v1")
                                .description(
                                        "Shipment document checklist, "
                                                + "verification and deletion APIs."
                                )
                                .contact(
                                        new Contact()
                                                .name("CargoSphere Team")
                                )
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        jwtSecurityScheme
                                )
                );
    }
}
