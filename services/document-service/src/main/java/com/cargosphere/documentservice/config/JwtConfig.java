package com.cargosphere.documentservice.config;

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
        byte[] decodedSecret = decodeSecret(
                jwtProperties.getSecret()
        );

        SecretKey secretKey = new SecretKeySpec(
                decodedSecret,
                "HmacSHA256"
        );

        NimbusJwtDecoder jwtDecoder =
                NimbusJwtDecoder.withSecretKey(secretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        jwtDecoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        jwtProperties.getIssuer()
                )
        );

        return jwtDecoder;
    }

    private byte[] decodeSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret must be configured"
            );
        }

        try {
            byte[] decodedSecret =
                    Base64.getDecoder().decode(secret);

            if (decodedSecret.length < MINIMUM_SECRET_BYTES) {
                throw new IllegalStateException(
                        "JWT secret must contain at least 32 decoded bytes"
                );
            }

            return decodedSecret;
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "JWT secret must be a valid Base64 value",
                    exception
            );
        }
    }
}