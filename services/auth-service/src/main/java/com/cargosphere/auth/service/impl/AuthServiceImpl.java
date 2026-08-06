package com.cargosphere.auth.service.impl;

import com.cargosphere.auth.audit.AuditClient;
import com.cargosphere.auth.audit.AuthAuditEventFactory;
import com.cargosphere.auth.dto.LoginRequest;
import com.cargosphere.auth.dto.LoginResponse;
import com.cargosphere.auth.dto.RegisterRequest;
import com.cargosphere.auth.dto.RegisterResponse;
import com.cargosphere.auth.dto.UserResponse;
import com.cargosphere.auth.dto.UpdateProfileRequest;
import com.cargosphere.auth.entity.Role;
import com.cargosphere.auth.entity.User;
import com.cargosphere.auth.entity.UserStatus;
import com.cargosphere.auth.exception.AccountNotActiveException;
import com.cargosphere.auth.exception.DuplicateResourceException;
import com.cargosphere.auth.exception.InvalidCredentialsException;
import com.cargosphere.auth.exception.ResourceNotFoundException;
import com.cargosphere.auth.mapper.AuthMapper;
import com.cargosphere.auth.repository.RoleRepository;
import com.cargosphere.auth.repository.UserRepository;
import com.cargosphere.auth.security.JwtService;
import com.cargosphere.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl
        implements AuthService {

    private static final String DEFAULT_ROLE =
            "ROLE_CLIENT";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditClient auditClient;
    private final AuthAuditEventFactory
            authAuditEventFactory;

    @Override
    public RegisterResponse register(
            RegisterRequest request
    ) {
        String normalizedEmail =
                request.email()
                        .trim()
                        .toLowerCase();

        if (userRepository.existsByEmail(
                normalizedEmail
        )) {
            throw new DuplicateResourceException(
                    "Email is already registered"
            );
        }

        Role clientRole =
                roleRepository
                        .findByName(DEFAULT_ROLE)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Default client role not found"
                                )
                        );

        User user = new User();

        user.setFullName(
                request.fullName().trim()
        );

        user.setEmail(normalizedEmail);

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.password()
                )
        );

        user.setPhoneNumber(
                normalizePhoneNumber(
                        request.phoneNumber()
                )
        );

        user.setRole(clientRole);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser =
                userRepository.save(user);

        RegisterResponse response =
                AuthMapper.toRegisterResponse(
                        savedUser
                );

        auditClient.publish(
                authAuditEventFactory
                        .userRegistered(savedUser)
        );

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(
            LoginRequest request
    ) {
        String normalizedEmail =
                request.email()
                        .trim()
                        .toLowerCase();

        User user =
                userRepository
                        .findByEmail(normalizedEmail)
                        .orElse(null);

        if (user == null) {
            publishLoginFailure(
                    null,
                    "User login failed due to invalid credentials",
                    401
            );

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            publishLoginFailure(
                    user,
                    "User login failed due to invalid credentials",
                    401
            );

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        if (user.getStatus()
                != UserStatus.ACTIVE) {

            publishLoginFailure(
                    user,
                    "User login failed because the account is not active",
                    403
            );

            throw new AccountNotActiveException(
                    "User account is not active"
            );
        }

        JwtService.GeneratedToken token =
                jwtService.generateToken(user);

        LoginResponse response =
                AuthMapper.toLoginResponse(
                        user,
                        token.value(),
                        token.expiresInSeconds()
                );

        auditClient.publish(
                authAuditEventFactory
                        .loginSucceeded(user)
        );

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(AuthMapper::toUserResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with id: "
                                                + id
                                )
                        );

        return AuthMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return getUserById(userId);
    }

    @Override
    public UserResponse updateProfile(
            Long userId,
            UpdateProfileRequest request
    ) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));

        user.setFullName(request.fullName().trim());
        user.setPhoneNumber(normalizePhoneNumber(request.phoneNumber()));

        return AuthMapper.toUserResponse(userRepository.save(user));
    }

    private void publishLoginFailure(
            User user,
            String description,
            int statusCode
    ) {
        auditClient.publish(
                authAuditEventFactory.loginFailed(
                        user,
                        description,
                        statusCode
                )
        );
    }

    private String normalizePhoneNumber(
            String phoneNumber
    ) {
        if (phoneNumber == null
                || phoneNumber.isBlank()) {

            return null;
        }

        return phoneNumber.trim();
    }
}
