package com.cargosphere.auth.dto;

public record HealthResponse(
        String service,
        String status
) {
}