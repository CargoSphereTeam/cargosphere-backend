package com.cargosphere.auth.service;

import com.cargosphere.auth.dto.LoginRequest;
import com.cargosphere.auth.dto.LoginResponse;
import com.cargosphere.auth.dto.RegisterRequest;
import com.cargosphere.auth.dto.RegisterResponse;
import com.cargosphere.auth.entity.Role;
import com.cargosphere.auth.entity.User;
import com.cargosphere.auth.entity.UserStatus;
import com.cargosphere.auth.exception.DuplicateResourceException;
import com.cargosphere.auth.exception.InvalidCredentialsException;
import com.cargosphere.auth.repository.RoleRepository;
import com.cargosphere.auth.repository.UserRepository;
import com.cargosphere.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerShouldCreateUserWhenEmailIsNotUsed() {
        RegisterRequest request = new RegisterRequest(
                "Dnyanesh Gholap",
                "DNYANESH@example.com",
                "Password@123",
                "9876543210"
        );

        Role role = new Role();
        role.setId(3L);
        role.setName("ROLE_CUSTOMER");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName("Dnyanesh Gholap");
        savedUser.setEmail("dnyanesh@example.com");
        savedUser.setPasswordHash("hashed-password");
        savedUser.setPhoneNumber("9876543210");
        savedUser.setRole(role);
        savedUser.setStatus(UserStatus.ACTIVE);
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());

        when(userRepository.existsByEmail("dnyanesh@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CUSTOMER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("Password@123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = authService.register(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("dnyanesh@example.com");
        assertThat(response.role()).isEqualTo("ROLE_CUSTOMER");
    }

    @Test
    void registerShouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "Dnyanesh Gholap",
                "dnyanesh@example.com",
                "Password@123",
                "9876543210"
        );

        when(userRepository.existsByEmail("dnyanesh@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email is already registered");
    }

    @Test
    void loginShouldReturnSuccessResponseWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest(
                "dnyanesh@example.com",
                "Password@123"
        );

        Role role = new Role();
        role.setId(3L);
        role.setName("ROLE_CUSTOMER");

        User user = new User();
        user.setId(1L);
        user.setFullName("Dnyanesh Gholap");
        user.setEmail("dnyanesh@example.com");
        user.setPasswordHash("hashed-password");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail("dnyanesh@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "hashed-password")).thenReturn(true);

        LoginResponse response = authService.login(request);

        assertThat(response.email()).isEqualTo("dnyanesh@example.com");
        assertThat(response.message()).isEqualTo("Login successful");
    }

    @Test
    void loginShouldThrowExceptionWhenPasswordIsWrong() {
        LoginRequest request = new LoginRequest(
                "dnyanesh@example.com",
                "WrongPassword"
        );

        Role role = new Role();
        role.setId(3L);
        role.setName("ROLE_CUSTOMER");

        User user = new User();
        user.setId(1L);
        user.setEmail("dnyanesh@example.com");
        user.setPasswordHash("hashed-password");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail("dnyanesh@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }
}