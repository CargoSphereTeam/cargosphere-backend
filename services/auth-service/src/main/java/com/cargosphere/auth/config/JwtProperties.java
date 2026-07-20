package com.cargosphere.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(

        @NotBlank(message = "JWT secret is required")
        String secret,

        @NotBlank(message = "JWT issuer is required")
        String issuer,

        @NotNull(message = "JWT expiration is required")
        Duration expiration
) {
}