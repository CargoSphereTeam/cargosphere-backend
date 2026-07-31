package com.cargosphere.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Tag(
        name = "Payment Service Health",
        description = "Payment-service availability endpoint"
)
public class PaymentHealthController {

    @Operation(
            summary = "Check payment-service health",
            description = "Returns the current payment-service availability."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Payment-service is running"
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "service", "payment-service",
                        "status", "UP"
                )
        );
    }
}
