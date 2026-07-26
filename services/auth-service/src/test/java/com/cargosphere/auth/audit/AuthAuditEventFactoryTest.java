package com.cargosphere.auth.audit;

import com.cargosphere.auth.entity.Role;
import com.cargosphere.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthAuditEventFactoryTest {

    private AuthAuditEventFactory factory;

    @BeforeEach
    void setUp() {
        factory = new AuthAuditEventFactory();
    }

    @Test
    void userRegisteredShouldCreateSuccessEvent() {
        AuditEventRequest event =
                factory.userRegistered(user());

        assertEquals(
                "USER_REGISTERED",
                event.action()
        );

        assertEquals(
                "USER",
                event.entityType()
        );

        assertEquals(
                "10",
                event.entityId()
        );

        assertEquals(
                "auth-service",
                event.serviceName()
        );

        assertEquals(
                "SUCCESS",
                event.outcome()
        );

        assertEquals(
                "/api/auth/register",
                event.endpoint()
        );

        assertEquals(
                201,
                event.statusCode()
        );
    }

    @Test
    void loginSucceededShouldCreateSuccessEvent() {
        AuditEventRequest event =
                factory.loginSucceeded(user());

        assertEquals(
                "USER_LOGIN",
                event.action()
        );

        assertEquals(
                "ROLE_CLIENT",
                event.actorRole()
        );

        assertEquals(
                "SUCCESS",
                event.outcome()
        );

        assertEquals(
                200,
                event.statusCode()
        );
    }

    @Test
    void unknownUserLoginFailureShouldNotExposeIdentity() {
        AuditEventRequest event =
                factory.loginFailed(
                        null,
                        "User login failed due to invalid credentials",
                        401
                );

        assertEquals(
                "USER_LOGIN_FAILED",
                event.action()
        );

        assertEquals(
                "FAILURE",
                event.outcome()
        );

        assertNull(event.actorUserId());
        assertNull(event.actorRole());
        assertNull(event.entityId());

        assertEquals(
                401,
                event.statusCode()
        );
    }

    private User user() {
        Role role = new Role();
        role.setId(2L);
        role.setName("ROLE_CLIENT");

        User user = new User();
        user.setId(10L);
        user.setRole(role);

        return user;
    }
}