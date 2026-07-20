package com.cargosphere.auth.controller;

import com.cargosphere.auth.config.SecurityConfig;
import com.cargosphere.auth.dto.LoginRequest;
import com.cargosphere.auth.dto.LoginResponse;
import com.cargosphere.auth.dto.RegisterRequest;
import com.cargosphere.auth.dto.RegisterResponse;
import com.cargosphere.auth.dto.UserResponse;
import com.cargosphere.auth.exception.GlobalExceptionHandler;
import com.cargosphere.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    /*
     * SecurityConfig enables OAuth2 Resource Server JWT validation.
     * This mock allows the MVC test context to start without loading
     * the real JwtConfig and JWT secret.
     */
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void registerShouldReturnCreated() throws Exception {
        RegisterResponse response = new RegisterResponse(
                1L,
                "Dnyanesh Gholap",
                "dnyanesh@example.com",
                "9876543210",
                "ROLE_CLIENT",
                "ACTIVE",
                LocalDateTime.now()
        );

        Mockito.when(
                authService.register(any(RegisterRequest.class))
        ).thenReturn(response);

        RegisterRequest request = new RegisterRequest(
                "Dnyanesh Gholap",
                "dnyanesh@example.com",
                "Password@123",
                "9876543210"
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Dnyanesh Gholap"))
                .andExpect(jsonPath("$.email").value("dnyanesh@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("9876543210"))
                .andExpect(jsonPath("$.role").value("ROLE_CLIENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void registerShouldReturnBadRequestForInvalidData() throws Exception {
        String invalidRequest = """
                {
                  "fullName": "",
                  "email": "wrong-email",
                  "password": "123",
                  "phoneNumber": "abc"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void loginShouldReturnOkAndJwtToken() throws Exception {
        LoginResponse response = new LoginResponse(
                1L,
                "Dnyanesh Gholap",
                "dnyanesh@example.com",
                "ROLE_CLIENT",
                "ACTIVE",
                "test-jwt-access-token",
                "Bearer",
                3600L,
                "Login successful"
        );

        Mockito.when(
                authService.login(any(LoginRequest.class))
        ).thenReturn(response);

        LoginRequest request = new LoginRequest(
                "dnyanesh@example.com",
                "Password@123"
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Dnyanesh Gholap"))
                .andExpect(jsonPath("$.email").value("dnyanesh@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_CLIENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.accessToken")
                        .value("test-jwt-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    @WithMockUser(
            username = "admin@cargosphere.com",
            roles = "ADMIN"
    )
    void getAllUsersShouldReturnOkForAdmin() throws Exception {
        UserResponse user = new UserResponse(
                1L,
                "Dnyanesh Gholap",
                "dnyanesh@example.com",
                "9876543210",
                "ROLE_CLIENT",
                "ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Mockito.when(authService.getAllUsers())
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email")
                        .value("dnyanesh@example.com"))
                .andExpect(jsonPath("$[0].role")
                        .value("ROLE_CLIENT"));
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void getAllUsersShouldReturnForbiddenForClient() throws Exception {
        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isForbidden());

        Mockito.verifyNoInteractions(authService);
    }

    @Test
    void getAllUsersShouldReturnUnauthorizedWithoutAuthentication()
            throws Exception {

        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isUnauthorized());

        Mockito.verifyNoInteractions(authService);
    }
}