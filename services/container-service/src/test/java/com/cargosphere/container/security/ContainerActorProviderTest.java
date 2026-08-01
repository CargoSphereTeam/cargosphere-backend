package com.cargosphere.container.security;

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

class ContainerActorProviderTest {

    private final ContainerActorProvider actorProvider =
            new ContainerActorProvider();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentActor_shouldReadNumericUserIdAndAdminRole() {
        Jwt jwt = createJwt(10L);

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        jwt,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        CurrentActor actor = actorProvider.getCurrentActor();

        assertEquals(10L, actor.userId());
        assertEquals("ROLE_ADMIN", actor.role());
    }

    @Test
    void getCurrentActor_shouldReadStringUserIdAndClientRole() {
        Jwt jwt = createJwt("25");

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        jwt,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_CLIENT")
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        CurrentActor actor = actorProvider.getCurrentActor();

        assertEquals(25L, actor.userId());
        assertEquals("ROLE_CLIENT", actor.role());
    }

    @Test
    void getCurrentActor_whenUserIdClaimMissing_shouldThrowException() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claim("authorities", List.of("ROLE_ADMIN"))
                .build();

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        jwt,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                actorProvider::getCurrentActor
        );

        assertEquals(
                "JWT userId claim is missing",
                exception.getMessage()
        );
    }

    @Test
    void getCurrentActor_whenUserIdIsNotNumeric_shouldThrowException() {
        Jwt jwt = createJwt("not-a-number");

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        jwt,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                actorProvider::getCurrentActor
        );

        assertEquals(
                "JWT userId claim must be numeric",
                exception.getMessage()
        );
    }

    @Test
    void getCurrentActor_whenRoleIsUnsupported_shouldThrowException() {
        Jwt jwt = createJwt(10L);

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        jwt,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_UNKNOWN")
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                actorProvider::getCurrentActor
        );

        assertEquals(
                "Authenticated actor has no supported role",
                exception.getMessage()
        );
    }

    @Test
    void getCurrentActor_whenAuthenticationMissing_shouldThrowException() {
        SecurityContextHolder.clearContext();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                actorProvider::getCurrentActor
        );

        assertEquals(
                "Authenticated JWT actor is required",
                exception.getMessage()
        );
    }

    private Jwt createJwt(Object userId) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claim("userId", userId)
                .claim("authorities", List.of("ROLE_ADMIN"))
                .build();
    }
}