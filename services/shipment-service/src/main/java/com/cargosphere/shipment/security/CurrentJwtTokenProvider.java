package com.cargosphere.shipment.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentJwtTokenProvider {

    public String getTokenValue() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (!(authentication
                instanceof JwtAuthenticationToken jwtAuthentication)
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authenticated JWT is required for downstream service calls"
            );
        }

        return jwtAuthentication
                .getToken()
                .getTokenValue();
    }
}
