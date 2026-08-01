package com.cargosphere.audit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/audits")
@Tag(
        name = "Audit Service Health",
        description = "Audit-service availability endpoint"
)
public class AuditHealthController {

    @Operation(
            summary = "Check audit-service health",
            description = "Returns the current audit-service availability."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Audit-service is running"
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "service", "audit-service",
                        "status", "UP"
                )
        );
    }
}
