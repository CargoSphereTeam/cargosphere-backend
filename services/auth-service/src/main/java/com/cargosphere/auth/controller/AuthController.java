package com.cargosphere.auth.controller;

import com.cargosphere.auth.config.OpenApiConfig;
import com.cargosphere.auth.dto.ErrorResponse;
import com.cargosphere.auth.dto.LoginRequest;
import com.cargosphere.auth.dto.LoginResponse;
import com.cargosphere.auth.dto.RegisterRequest;
import com.cargosphere.auth.dto.RegisterResponse;
import com.cargosphere.auth.dto.UserResponse;
import com.cargosphere.auth.dto.UpdateProfileRequest;
import com.cargosphere.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication and Users",
        description =
                "Client registration, login and ADMIN user management"
)
public class AuthController {

    private final AuthService authService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                authService.getProfile(jwt.getClaim("userId"))
        );
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(
                authService.updateProfile(jwt.getClaim("userId"), request)
        );
    }

    @Operation(
            summary = "Register a client account",
            description =
                    "Creates a new CargoSphere account with ROLE_CLIENT."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Client registered successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            RegisterResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Log in",
            description =
                    "Validates account credentials and returns a JWT "
                            + "access token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            LoginResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Account is not active",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all users",
            description =
                    "Returns every registered user. Requires ROLE_ADMIN."
    )
    @SecurityRequirement(
            name = OpenApiConfig.SECURITY_SCHEME_NAME
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> getAllUsers() {
        return authService.getAllUsers();
    }

    @Operation(
            summary = "Get user by ID",
            description =
                    "Returns one registered user. Requires ROLE_ADMIN."
    )
    @SecurityRequirement(
            name = OpenApiConfig.SECURITY_SCHEME_NAME
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User returned successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            UserResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(
                    description = "User ID",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    ) {
        UserResponse response =
                authService.getUserById(id);

        return ResponseEntity.ok(response);
    }
}
