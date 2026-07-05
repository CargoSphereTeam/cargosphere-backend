package com.cargosphere.shipment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ShipmentHealthController {

    @GetMapping("/api/shipments/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "shipment-service",
                "status", "UP"
        );
    }
}