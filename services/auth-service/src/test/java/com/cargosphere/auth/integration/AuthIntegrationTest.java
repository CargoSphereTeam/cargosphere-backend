package com.cargosphere.auth.integration;

import com.cargosphere.auth.dto.LoginRequest;
import com.cargosphere.auth.dto.RegisterRequest;
import com.cargosphere.auth.entity.Role;
import com.cargosphere.auth.entity.User;
import com.cargosphere.auth.entity.UserStatus;
import com.cargosphere.auth.repository.RoleRepository;
import com.cargosphere.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    private static final String PASSWORD = "Password@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registeredClientShouldLoginAndReceiveJwtButCannotAccessAdminEndpoint()
            throws Exception {

        String email = uniqueEmail("client");

        JsonNode registerResponse = registerUser(
                "Integration Client",
                email,
                PASSWORD
        );

        long userId = registerResponse.get("id").asLong();

        assertThat(registerResponse.get("email").asText())
                .isEqualTo(email);

        assertThat(registerResponse.get("role").asText())
                .isEqualTo("ROLE_CLIENT");

        assertThat(registerResponse.get("status").asText())
                .isEqualTo("ACTIVE");

        JsonNode loginResponse = login(email, PASSWORD);

        String accessToken = loginResponse
                .get("accessToken")
                .asText();

        assertThat(loginResponse.get("email").asText())
                .isEqualTo(email);

        assertThat(loginResponse.get("role").asText())
                .isEqualTo("ROLE_CLIENT");

        assertThat(loginResponse.get("tokenType").asText())
                .isEqualTo("Bearer");

        assertThat(loginResponse.get("expiresIn").asLong())
                .isPositive();

        assertThat(accessToken)
                .isNotBlank();

        assertThat(loginResponse.get("message").asText())
                .isEqualTo("Login successful");

        // The request is authenticated, but ROLE_CLIENT cannot access
        // the administrator-only users endpoint.
        mockMvc.perform(
                        get("/api/auth/users/" + userId)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminJwtShouldAllowAccessToUserById() throws Exception {

        String clientEmail = uniqueEmail("target-client");

        JsonNode registerResponse = registerUser(
                "Target Client",
                clientEmail,
                PASSWORD
        );

        long clientUserId = registerResponse
                .get("id")
                .asLong();

        String adminEmail = uniqueEmail("admin");

        createAdminUser(
                "Integration Administrator",
                adminEmail,
                PASSWORD
        );

        JsonNode adminLoginResponse = login(
                adminEmail,
                PASSWORD
        );

        String adminAccessToken = adminLoginResponse
                .get("accessToken")
                .asText();

        assertThat(adminLoginResponse.get("role").asText())
                .isEqualTo("ROLE_ADMIN");

        assertThat(adminAccessToken)
                .isNotBlank();

        mockMvc.perform(
                        get("/api/auth/users/" + clientUserId)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + adminAccessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientUserId))
                .andExpect(jsonPath("$.email").value(clientEmail))
                .andExpect(jsonPath("$.role").value("ROLE_CLIENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void usersEndpointShouldRejectRequestWithoutJwt()
            throws Exception {

        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode registerUser(
            String fullName,
            String email,
            String password
    ) throws Exception {

        RegisterRequest request = new RegisterRequest(
                fullName,
                email,
                password,
                "9876543210"
        );

        String responseJson = mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseJson);
    }

    private JsonNode login(
            String email,
            String password
    ) throws Exception {

        LoginRequest request = new LoginRequest(
                email,
                password
        );

        String responseJson = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseJson);
    }

    private void createAdminUser(
            String fullName,
            String email,
            String password
    ) {
        Role adminRole = roleRepository
                .findByName("ROLE_ADMIN")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "ROLE_ADMIN is missing from the test database"
                        )
                );

        User admin = new User();
        admin.setFullName(fullName);
        admin.setEmail(email.toLowerCase());
        admin.setPasswordHash(
                passwordEncoder.encode(password)
        );
        admin.setPhoneNumber("9999999999");
        admin.setRole(adminRole);
        admin.setStatus(UserStatus.ACTIVE);

        userRepository.saveAndFlush(admin);
    }

    private String uniqueEmail(String prefix) {
        return prefix
                + "-"
                + UUID.randomUUID()
                + "@example.com";
    }
}