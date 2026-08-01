package com.cargosphere.container.security;

public record CurrentActor(
        Long userId,
        String role
) {
}