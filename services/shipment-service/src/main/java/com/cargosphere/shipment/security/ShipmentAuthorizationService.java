package com.cargosphere.shipment.security;

import com.cargosphere.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component("shipmentAuthorizationService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentAuthorizationService {

    private final ShipmentRepository shipmentRepository;

    public boolean isCurrentUser(
            Long clientUserId,
            Authentication authentication
    ) {
        Long authenticatedUserId =
                extractUserId(authentication);

        return authenticatedUserId != null
                && Objects.equals(
                        clientUserId,
                        authenticatedUserId
                );
    }

    public boolean isShipmentOwner(
            Long shipmentId,
            Authentication authentication
    ) {
        Long authenticatedUserId =
                extractUserId(authentication);

        return authenticatedUserId != null
                && shipmentRepository
                .existsByIdAndClientUserId(
                        shipmentId,
                        authenticatedUserId
                );
    }

    public boolean isShipmentNumberOwner(
            String shipmentNumber,
            Authentication authentication
    ) {
        Long authenticatedUserId =
                extractUserId(authentication);

        return authenticatedUserId != null
                && shipmentRepository
                .existsByShipmentNumberAndClientUserId(
                        shipmentNumber,
                        authenticatedUserId
                );
    }

    private Long extractUserId(
            Authentication authentication
    ) {
        if (!(authentication
                instanceof JwtAuthenticationToken jwtAuthentication)) {
            return null;
        }

        Object userIdClaim = jwtAuthentication
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
}