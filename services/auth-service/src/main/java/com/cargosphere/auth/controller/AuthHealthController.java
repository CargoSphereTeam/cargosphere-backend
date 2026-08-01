package com.cargosphere.auth.controller;

import com.cargosphere.auth.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Auth Service Health",
        description = "Auth-service availability endpoint"
)
public class AuthHealthController {

    @Operation(
            summary = "Check auth-service health",
            description =
                    "Returns the current auth-service availability status."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Auth-service is running"
    )
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(
                new HealthResponse(
                        "auth-service",
                        "UP"
                )
        );
    }
}