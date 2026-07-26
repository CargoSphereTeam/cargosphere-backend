package com.cargosphere.auth.service.impl;

import com.cargosphere.auth.audit.AuditClient;
import com.cargosphere.auth.audit.AuditEventRequest;
import com.cargosphere.auth.audit.AuthAuditEventFactory;
import com.cargosphere.auth.dto.LoginRequest;
import com.cargosphere.auth.dto.RegisterRequest;
import com.cargosphere.auth.entity.Role;
import com.cargosphere.auth.entity.User;
import com.cargosphere.auth.entity.UserStatus;
import com.cargosphere.auth.exception.InvalidCredentialsException;
import com.cargosphere.auth.repository.RoleRepository;
import com.cargosphere.auth.repository.UserRepository;
import com.cargosphere.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplAuditTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditClient auditClient;

    @Mock
    private AuthAuditEventFactory
            authAuditEventFactory;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerShouldPublishUserRegisteredEvent() {
        Role role = role();

        User savedUser = user();
        savedUser.setId(10L);

        AuditEventRequest expectedEvent =
                event("USER_REGISTERED");

        when(userRepository.existsByEmail(
                "client@example.com"
        )).thenReturn(false);

        when(roleRepository.findByName(
                "ROLE_CLIENT"
        )).thenReturn(Optional.of(role));

        when(passwordEncoder.encode(
                "Password@123"
        )).thenReturn("encoded-password");

        when(userRepository.save(
                org.mockito.ArgumentMatchers.any(
                        User.class
                )
        )).thenReturn(savedUser);

        when(authAuditEventFactory
                .userRegistered(savedUser))
                .thenReturn(expectedEvent);

        authService.register(
                new RegisterRequest(
                        "Test Client",
                        "client@example.com",
                        "Password@123",
                        "9876543210"
                )
        );

        verify(auditClient)
                .publish(expectedEvent);
    }

    @Test
    void successfulLoginShouldPublishUserLoginEvent() {
        User user = user();

        AuditEventRequest expectedEvent =
                event("USER_LOGIN");

        JwtService.GeneratedToken token =
                mock(
                        JwtService
                                .GeneratedToken.class
                );

        when(userRepository.findByEmail(
                "client@example.com"
        )).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password@123",
                user.getPasswordHash()
        )).thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn(token);

        when(token.value())
                .thenReturn("jwt-token");

        when(token.expiresInSeconds())
                .thenReturn(3600L);

        when(authAuditEventFactory
                .loginSucceeded(user))
                .thenReturn(expectedEvent);

        authService.login(
                new LoginRequest(
                        "client@example.com",
                        "Password@123"
                )
        );

        verify(auditClient)
                .publish(expectedEvent);
    }

    @Test
    void unknownUserLoginShouldPublishFailureEvent() {
        AuditEventRequest expectedEvent =
                event("USER_LOGIN_FAILED");

        when(userRepository.findByEmail(
                "missing@example.com"
        )).thenReturn(Optional.empty());

        when(authAuditEventFactory.loginFailed(
                null,
                "User login failed due to invalid credentials",
                401
        )).thenReturn(expectedEvent);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(
                        new LoginRequest(
                                "missing@example.com",
                                "WrongPassword"
                        )
                )
        );

        verify(auditClient)
                .publish(expectedEvent);
    }

    private Role role() {
        Role role = new Role();

        role.setId(2L);
        role.setName("ROLE_CLIENT");

        return role;
    }

    private User user() {
        User user = new User();

        user.setId(10L);
        user.setFullName("Test Client");
        user.setEmail("client@example.com");
        user.setPasswordHash("encoded-password");
        user.setPhoneNumber("9876543210");
        user.setRole(role());
        user.setStatus(UserStatus.ACTIVE);

        return user;
    }

    private AuditEventRequest event(
            String action
    ) {
        return new AuditEventRequest(
                10L,
                "ROLE_CLIENT",
                action,
                "USER",
                "10",
                "auth-service",
                "Authentication audit event",
                "SUCCESS",
                null,
                null,
                "POST",
                "/api/auth/login",
                200
        );
    }
}