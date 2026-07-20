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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

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

        Mockito.when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        RegisterRequest request = new RegisterRequest(
                "Dnyanesh Gholap",
                "dnyanesh@example.com",
                "Password@123",
                "9876543210"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("dnyanesh@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_CLIENT"));
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

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void loginShouldReturnOk() throws Exception {
        LoginResponse response = new LoginResponse(
                1L,
                "Dnyanesh Gholap",
                "dnyanesh@example.com",
                "ROLE_CLIENT",
                "ACTIVE",
                "Login successful"
        );

        Mockito.when(authService.login(any(LoginRequest.class))).thenReturn(response);

        LoginRequest request = new LoginRequest(
                "dnyanesh@example.com",
                "Password@123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("dnyanesh@example.com"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void getAllUsersShouldReturnOk() throws Exception {
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

        Mockito.when(authService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("dnyanesh@example.com"));
    }
}
