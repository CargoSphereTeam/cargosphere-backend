package com.cargosphere.auth.audit;

import com.cargosphere.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthAuditEventFactory {

    private static final String SERVICE_NAME =
            "auth-service";

    private static final String USER_ENTITY =
            "USER";

    private static final String SUCCESS =
            "SUCCESS";

    private static final String FAILURE =
            "FAILURE";

    public AuditEventRequest userRegistered(
            User user
    ) {
        return create(
                user,
                "USER_REGISTERED",
                "User registered successfully",
                SUCCESS,
                "/api/auth/register",
                201
        );
    }

    public AuditEventRequest loginSucceeded(
            User user
    ) {
        return create(
                user,
                "USER_LOGIN",
                "User login succeeded",
                SUCCESS,
                "/api/auth/login",
                200
        );
    }

    public AuditEventRequest loginFailed(
            User user,
            String description,
            int statusCode
    ) {
        return create(
                user,
                "USER_LOGIN_FAILED",
                description,
                FAILURE,
                "/api/auth/login",
                statusCode
        );
    }

    private AuditEventRequest create(
            User user,
            String action,
            String description,
            String outcome,
            String endpoint,
            int statusCode
    ) {
        return new AuditEventRequest(
                user == null
                        ? null
                        : user.getId(),

                extractRole(user),

                action,

                USER_ENTITY,

                user == null
                        || user.getId() == null
                        ? null
                        : user.getId().toString(),

                SERVICE_NAME,

                description,

                outcome,

                null,

                null,

                "POST",

                endpoint,

                statusCode
        );
    }

    private String extractRole(User user) {
        if (user == null
                || user.getRole() == null) {

            return null;
        }

        return user.getRole().getName();
    }
}