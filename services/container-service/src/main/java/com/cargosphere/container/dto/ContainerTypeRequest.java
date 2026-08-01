package com.cargosphere.container.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(
        name = "ContainerTypeRequest",
        description =
                "Request body used to create or update a container type"
)
public record ContainerTypeRequest(

        @NotBlank(message = "Type code is required")
        @Size(max = 30, message = "Type code cannot exceed 30 characters")
        @Schema(
                description = "Unique container type code",
                example = "40HC",
                maxLength = 30,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String typeCode,

        @NotBlank(message = "Type name is required")
        @Size(max = 100, message = "Type name cannot exceed 100 characters")
        @Schema(
                description = "Human-readable container type name",
                example = "40 Foot High Cube",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String typeName,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        @Schema(
                description = "Optional container description",
                example = "High-capacity dry cargo container",
                maxLength = 255
        )
        String description,

        @NotNull(message = "Maximum weight is required")
        @DecimalMin(
                value = "0.01",
                message = "Maximum weight must be greater than zero"
        )
        @Schema(
                description = "Maximum supported weight in kilograms",
                example = "28000.00",
                minimum = "0.01",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        BigDecimal maxWeightKg,

        @NotNull(message = "Maximum volume is required")
        @DecimalMin(
                value = "0.01",
                message = "Maximum volume must be greater than zero"
        )
        @Schema(
                description = "Maximum supported volume in cubic metres",
                example = "76.40",
                minimum = "0.01",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        BigDecimal maxVolumeCbm,

        @DecimalMin(
                value = "0.01",
                message = "Length must be greater than zero"
        )
        @Schema(
                description = "Container length in metres",
                example = "12.03",
                minimum = "0.01"
        )
        BigDecimal lengthM,

        @DecimalMin(
                value = "0.01",
                message = "Width must be greater than zero"
        )
        @Schema(
                description = "Container width in metres",
                example = "2.35",
                minimum = "0.01"
        )
        BigDecimal widthM,

        @DecimalMin(
                value = "0.01",
                message = "Height must be greater than zero"
        )
        @Schema(
                description = "Container height in metres",
                example = "2.69",
                minimum = "0.01"
        )
        BigDecimal heightM,

        @Schema(
                description = "Whether the container type is active",
                example = "true"
        )
        Boolean active
) {
}
