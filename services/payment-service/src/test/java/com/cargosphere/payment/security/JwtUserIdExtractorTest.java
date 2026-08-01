package com.cargosphere.payment.security;

import com.cargosphere.payment.exception.InvalidJwtClaimException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUserIdExtractorTest {

    private JwtUserIdExtractor jwtUserIdExtractor;

    @BeforeEach
    void setUp() {
        jwtUserIdExtractor = new JwtUserIdExtractor();
    }

    @Test
    void extractUserIdShouldReturnIdWhenClaimIsNumber() {
        Jwt jwt = jwtWithUserId(101L);

        Long userId =
                jwtUserIdExtractor.extractUserId(jwt);

        assertEquals(101L, userId);
    }

    @Test
    void extractUserIdShouldReturnIdWhenClaimIsString() {
        Jwt jwt = jwtWithUserId("101");

        Long userId =
                jwtUserIdExtractor.extractUserId(jwt);

        assertEquals(101L, userId);
    }

    @Test
    void extractUserIdShouldThrowWhenClaimIsMissing() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .issuedAt(Instant.now())
                .expiresAt(
                        Instant.now().plusSeconds(300)
                )
                .claim("sub", "client@example.com")
                .build();

        InvalidJwtClaimException exception =
                assertThrows(
                        InvalidJwtClaimException.class,
                        () -> jwtUserIdExtractor
                                .extractUserId(jwt)
                );

        assertEquals(
                "JWT does not contain a valid userId claim",
                exception.getMessage()
        );
    }

    @Test
    void extractUserIdShouldThrowWhenClaimIsNotPositive() {
        Jwt jwt = jwtWithUserId(0L);

        InvalidJwtClaimException exception =
                assertThrows(
                        InvalidJwtClaimException.class,
                        () -> jwtUserIdExtractor
                                .extractUserId(jwt)
                );

        assertEquals(
                "JWT userId claim must be positive",
                exception.getMessage()
        );
    }

    private Jwt jwtWithUserId(Object userId) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .issuedAt(Instant.now())
                .expiresAt(
                        Instant.now().plusSeconds(300)
                )
                .claim("sub", "client@example.com")
                .claim("userId", userId)
                .build();
    }
}