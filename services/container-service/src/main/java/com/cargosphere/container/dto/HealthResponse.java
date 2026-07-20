package com.cargosphere.container.dto;

public record HealthResponse(
        String service,
        String status
) {
}