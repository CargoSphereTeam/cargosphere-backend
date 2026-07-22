package com.cargosphere.shipment.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(

        @NotBlank(message = "JWT secret is required")
        String secret,

        @NotBlank(message = "JWT issuer is required")
        String issuer
) {
}