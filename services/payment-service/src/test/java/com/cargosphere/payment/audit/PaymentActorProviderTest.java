package com.cargosphere.payment.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentActorProviderTest {

    private final PaymentActorProvider provider =
            new PaymentActorProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldExtractNumericUserIdAndClientRole() {
        setAuthentication(
                10L,
                "ROLE_CLIENT"
        );

        CurrentActor actor =
                provider.getCurrentActor();

        assertEquals(10L, actor.userId());

        assertEquals(
                "ROLE_CLIENT",
                actor.role()
        );
    }

    @Test
    void shouldExtractStringUserIdAndAdminRole() {
        setAuthentication(
                "1",
                "ROLE_ADMIN"
        );

        CurrentActor actor =
                provider.getCurrentActor();

        assertEquals(1L, actor.userId());

        assertEquals(
                "ROLE_ADMIN",
                actor.role()
        );
    }

    @Test
    void missingAuthenticationShouldReturnAnonymous() {
        CurrentActor actor =
                provider.getCurrentActor();

        assertNull(actor.userId());
        assertNull(actor.role());
    }

    @Test
    void invalidUserIdShouldReturnNullUserId() {
        setAuthentication(
                "invalid",
                "ROLE_ADMIN"
        );

        CurrentActor actor =
                provider.getCurrentActor();

        assertNull(actor.userId());

        assertEquals(
                "ROLE_ADMIN",
                actor.role()
        );
    }

    private void setAuthentication(
            Object userId,
            String role
    ) {
        Instant now = Instant.now();

        Jwt jwt = Jwt
                .withTokenValue("test-token")
                .header("alg", "none")
                .subject("test@example.com")
                .issuedAt(now)
                .expiresAt(
                        now.plusSeconds(3600)
                )
                .claim("userId", userId)
                .claim(
                        "authorities",
                        List.of(role)
                )
                .build();

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        jwt,
                        List.of(
                                new SimpleGrantedAuthority(
                                        role
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}