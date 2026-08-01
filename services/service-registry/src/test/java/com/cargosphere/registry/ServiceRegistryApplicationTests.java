package com.cargosphere.registry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ServiceRegistryApplicationTests {

    @Test
    void applicationMainMethodShouldBePresent() {
        assertDoesNotThrow(() ->
                ServiceRegistryApplication.class
                        .getDeclaredMethod(
                                "main",
                                String[].class
                        )
        );
    }
}