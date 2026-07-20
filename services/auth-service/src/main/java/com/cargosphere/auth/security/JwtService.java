package com.cargosphere.auth.security;

import com.cargosphere.auth.config.JwtProperties;
import com.cargosphere.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public GeneratedToken generateToken(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.expiration());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim(
                        "authorities",
                        List.of(user.getRole().getName())
                )
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String tokenValue = jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        return new GeneratedToken(
                tokenValue,
                jwtProperties.expiration().toSeconds()
        );
    }

    public record GeneratedToken(
            String value,
            long expiresInSeconds
    ) {
    }
}