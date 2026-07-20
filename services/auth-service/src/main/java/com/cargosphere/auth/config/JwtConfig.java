package com.cargosphere.auth.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private static final int MINIMUM_KEY_LENGTH_BYTES = 32;

    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {
        byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(properties.secret());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "JWT_SECRET must be a valid Base64-encoded value",
                    exception
            );
        }

        if (keyBytes.length < MINIMUM_KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET must contain at least 32 bytes after Base64 decoding"
            );
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        JWKSource<SecurityContext> jwkSource =
                new ImmutableSecret<>(jwtSecretKey);

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            JwtProperties properties
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(properties.issuer())
        );

        return decoder;
    }
}