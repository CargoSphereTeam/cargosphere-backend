package com.cargosphere.container.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class ContainerActorProvider {

    private static final String USER_ID_CLAIM = "userId";

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private static final String ROLE_CLIENT = "ROLE_CLIENT";

    public CurrentActor getCurrentActor() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtToken)
                || !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "Authenticated JWT actor is required"
            );
        }

        Long userId = extractUserId(jwtToken);

        String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority ->
                        ROLE_ADMIN.equals(authority)
                                || ROLE_CLIENT.equals(authority)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated actor has no supported role"
                        )
                );

        return new CurrentActor(userId, role);
    }

    private Long extractUserId(
            JwtAuthenticationToken jwtToken
    ) {
        Object claimValue =
                jwtToken.getToken().getClaims().get(USER_ID_CLAIM);

        if (claimValue instanceof Number number) {
            return number.longValue();
        }

        if (claimValue instanceof String value
                && !value.isBlank()) {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException exception) {
                throw new IllegalStateException(
                        "JWT userId claim must be numeric",
                        exception
                );
            }
        }

        throw new IllegalStateException(
                "JWT userId claim is missing"
        );
    }
}