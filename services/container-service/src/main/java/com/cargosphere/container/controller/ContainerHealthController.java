package com.cargosphere.container.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/containers")
@Tag(
        name = "Container Service Health",
        description = "Container-service availability endpoint"
)
public class ContainerHealthController {

    @Operation(
            summary = "Check container-service health",
            description =
                    "Returns the current container-service availability."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Container-service is running"
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "service", "container-service",
                        "status", "UP"
                )
        );
    }
}
