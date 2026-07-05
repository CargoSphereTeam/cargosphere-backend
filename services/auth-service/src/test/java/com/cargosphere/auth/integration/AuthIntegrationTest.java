package com.cargosphere.auth.integration;

import com.cargosphere.auth.dto.LoginRequest;
import com.cargosphere.auth.dto.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginAndGetUserByIdShouldWork() throws Exception {
        String email = "integration-" + UUID.randomUUID() + "@example.com";
        String password = "Password@123";

        RegisterRequest registerRequest = new RegisterRequest(
                "Integration Test User",
                email,
                password,
                "9876543210"
        );

        String registerResponseJson = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode registerResponse = objectMapper.readTree(registerResponseJson);

        Long userId = registerResponse.get("id").asLong();

        assertThat(registerResponse.get("email").asText()).isEqualTo(email);
        assertThat(registerResponse.get("role").asText()).isEqualTo("ROLE_CUSTOMER");
        assertThat(registerResponse.get("status").asText()).isEqualTo("ACTIVE");

        LoginRequest loginRequest = new LoginRequest(
                email,
                password
        );

        String loginResponseJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginResponse = objectMapper.readTree(loginResponseJson);

        assertThat(loginResponse.get("email").asText()).isEqualTo(email);
        assertThat(loginResponse.get("message").asText()).isEqualTo("Login successful");

        String getUserResponseJson = mockMvc.perform(get("/api/auth/users/" + userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode getUserResponse = objectMapper.readTree(getUserResponseJson);

        assertThat(getUserResponse.get("id").asLong()).isEqualTo(userId);
        assertThat(getUserResponse.get("email").asText()).isEqualTo(email);
    }
}