package com.cargosphere.shipment.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentJwtTokenProviderTest {

    private final CurrentJwtTokenProvider provider =
            new CurrentJwtTokenProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnCurrentJwtTokenValue() {
        Instant issuedAt = Instant.now();

        Jwt jwt = Jwt
                .withTokenValue("admin-test-token")
                .header("alg", "none")
                .subject("admin@example.com")
                .issuedAt(issuedAt)
                .expiresAt(
                        issuedAt.plusSeconds(3600)
                )
                .build();

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        jwt,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        assertEquals(
                "admin-test-token",
                provider.getTokenValue()
        );
    }

    @Test
    void missingAuthenticationShouldThrowException() {
        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        provider::getTokenValue
                );

        assertEquals(
                "Authenticated JWT is required for downstream service calls",
                exception.getMessage()
        );
    }
}
