package com.cargosphere.shipment.audit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class ShipmentActorProvider {

    public CurrentActor getCurrentActor() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (!(authentication
                instanceof JwtAuthenticationToken jwtAuthentication)
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
        Object userIdClaim =
                authentication
                        .getToken()
                        .getClaim("userId");

        if (userIdClaim instanceof Number number) {
            return number.longValue();
        }

        if (userIdClaim instanceof String value) {
            try {
                return Long.valueOf(value);
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
                .map(GrantedAuthority::getAuthority)
                .filter(authority ->
                        authority.startsWith("ROLE_")
                )
                .findFirst()
                .orElse(null);
    }
}