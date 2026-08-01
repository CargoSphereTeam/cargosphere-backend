package com.cargosphere.shipment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(
        name = "Shipment Service Health",
        description = "Shipment-service availability endpoint"
)
public class ShipmentHealthController {

    @Operation(
            summary = "Check shipment-service health",
            description =
                    "Returns the current shipment-service availability."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Shipment-service is running"
    )
    @GetMapping("/api/shipments/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "shipment-service",
                "status", "UP"
        );
    }
}
