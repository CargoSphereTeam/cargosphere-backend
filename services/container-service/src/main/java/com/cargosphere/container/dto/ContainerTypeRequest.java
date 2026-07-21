package com.cargosphere.container.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ContainerTypeRequest(

        @NotBlank(message = "Type code is required")
        @Size(max = 30, message = "Type code cannot exceed 30 characters")
        String typeCode,

        @NotBlank(message = "Type name is required")
        @Size(max = 100, message = "Type name cannot exceed 100 characters")
        String typeName,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,

        @NotNull(message = "Maximum weight is required")
        @DecimalMin(value = "0.01", message = "Maximum weight must be greater than zero")
        BigDecimal maxWeightKg,

        @NotNull(message = "Maximum volume is required")
        @DecimalMin(value = "0.01", message = "Maximum volume must be greater than zero")
        BigDecimal maxVolumeCbm,

        @DecimalMin(value = "0.01", message = "Length must be greater than zero")
        BigDecimal lengthM,

        @DecimalMin(value = "0.01", message = "Width must be greater than zero")
        BigDecimal widthM,

        @DecimalMin(value = "0.01", message = "Height must be greater than zero")
        BigDecimal heightM,

        Boolean active
) {
}