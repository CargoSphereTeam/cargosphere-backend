package com.cargosphere.payment.audit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class PaymentActorProvider {

    public CurrentActor getCurrentActor() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (!(authentication
                instanceof JwtAuthenticationToken
                jwtAuthentication)
                || !authentication.isAuthenticated()) {

            return CurrentActor.anonymous();
        }

        return new CurrentActor(
                extractUserId(jwtAuthentication),
                extractRole(authentication)
        );
    }

    private Long extractUserId(
            JwtAuthenticationToken authentication
    ) {
        Object claim =
                authentication
                        .getToken()
                        .getClaim("userId");

        if (claim instanceof Number number) {
            long userId = number.longValue();

            return userId > 0
                    ? userId
                    : null;
        }

        if (claim instanceof String value) {
            try {
                long userId =
                        Long.parseLong(
                                value.trim()
                        );

                return userId > 0
                        ? userId
                        : null;

            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private String extractRole(
            Authentication authentication
    ) {
        return authentication
                .getAuthorities()
                .stream()
                .map(
                        GrantedAuthority::getAuthority
                )
                .filter(authority ->
                        authority.startsWith(
                                "ROLE_"
                        )
                )
                .findFirst()
                .orElse(null);
    }
}