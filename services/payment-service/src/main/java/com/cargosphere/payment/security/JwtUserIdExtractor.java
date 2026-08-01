package com.cargosphere.payment.security;

import com.cargosphere.payment.exception.InvalidJwtClaimException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtUserIdExtractor {

    private static final String USER_ID_CLAIM = "userId";

    public Long extractUserId(Jwt jwt) {
        if (jwt == null) {
            throw new InvalidJwtClaimException(
                    "Authenticated JWT is required"
            );
        }

        Object claimValue =
                jwt.getClaims().get(USER_ID_CLAIM);

        if (claimValue instanceof Number number) {
            long userId = number.longValue();

            validatePositiveUserId(userId);
            return userId;
        }

        if (claimValue instanceof String value) {
            return parseUserId(value);
        }

        throw new InvalidJwtClaimException(
                "JWT does not contain a valid userId claim"
        );
    }

    private Long parseUserId(String value) {
        try {
            long userId = Long.parseLong(value.trim());

            validatePositiveUserId(userId);
            return userId;
        } catch (NumberFormatException exception) {
            throw new InvalidJwtClaimException(
                    "JWT userId claim must be a valid number"
            );
        }
    }

    private void validatePositiveUserId(long userId) {
        if (userId <= 0) {
            throw new InvalidJwtClaimException(
                    "JWT userId claim must be positive"
            );
        }
    }
}