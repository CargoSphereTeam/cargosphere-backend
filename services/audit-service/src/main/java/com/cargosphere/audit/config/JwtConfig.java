package com.cargosphere.audit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private static final int MINIMUM_SECRET_BYTES = 32;

    @Bean
    public JwtDecoder jwtDecoder(
            JwtProperties jwtProperties
    ) {
        byte[] decodedSecret =
                decodeSecret(jwtProperties.getSecret());

        String issuer =
                validateIssuer(jwtProperties.getIssuer());

        SecretKey secretKey = new SecretKeySpec(
                decodedSecret,
                "HmacSHA256"
        );

        NimbusJwtDecoder jwtDecoder =
                NimbusJwtDecoder
                        .withSecretKey(secretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        jwtDecoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        issuer
                )
        );

        return jwtDecoder;
    }

    private byte[] decodeSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret is not configured"
            );
        }

        byte[] decodedSecret;

        try {
            decodedSecret = Base64
                    .getDecoder()
                    .decode(secret.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "JWT secret must be valid Base64",
                    exception
            );
        }

        if (decodedSecret.length
                < MINIMUM_SECRET_BYTES) {

            throw new IllegalStateException(
                    "JWT secret must contain at least "
                            + MINIMUM_SECRET_BYTES
                            + " decoded bytes"
            );
        }

        return decodedSecret;
    }

    private String validateIssuer(String issuer) {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException(
                    "JWT issuer is not configured"
            );
        }

        return issuer.trim();
    }
}