package com.cargosphere.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @Pattern(
                regexp = "^$|^[0-9]{10,15}$",
                message = "Phone number must contain 10 to 15 digits"
        )
        String phoneNumber
) {
}
