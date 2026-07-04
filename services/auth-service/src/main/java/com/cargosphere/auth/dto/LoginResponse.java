package com.cargosphere.auth.dto;

public record LoginResponse(
        Long id,
        String fullName,
        String email,
        String role,
        String status,
        String message
) {
}