package com.cargosphere.shipment.dto;

import com.cargosphere.shipment.entity.enums.CargoType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "CargoDetailRequest",
        description = "Request body used to add cargo details to a shipment"
)
public class CargoDetailRequest {

    @NotBlank(message = "Cargo name is required")
    @Size(
            max = 100,
            message = "Cargo name must not exceed 100 characters"
    )
    @Schema(
            description = "Name of the cargo item",
            example = "Electronics Box",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String cargoName;

    @Size(
            max = 255,
            message = "Cargo description must not exceed 255 characters"
    )
    @Schema(
            description = "Optional description of the cargo",
            example = "Laptop accessories packed in protective boxes",
            maxLength = 255
    )
    private String cargoDescription;

    @Schema(
            description = "Classification of the cargo",
            example = "ELECTRONICS",
            allowableValues = {
                    "GENERAL",
                    "FRAGILE",
                    "HAZARDOUS",
                    "PERISHABLE",
                    "LIQUID",
                    "HEAVY",
                    "ELECTRONICS",
                    "OTHER"
            }
    )
    private CargoType cargoType;

    @NotNull(message = "Cargo weight is required")
    @DecimalMin(
            value = "0.001",
            message = "Cargo weight must be at least 0.001 kg"
    )
    @Digits(
            integer = 10,
            fraction = 3,
            message =
                    "Cargo weight can contain up to 10 integer digits "
                            + "and 3 decimal digits"
    )
    @Schema(
            description = "Cargo weight in kilograms",
            example = "25.500",
            minimum = "0.001",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal weightKg;

    @DecimalMin(
            value = "0.001",
            message = "Cargo volume must be at least 0.001 CBM"
    )
    @Digits(
            integer = 10,
            fraction = 3,
            message =
                    "Cargo volume can contain up to 10 integer digits "
                            + "and 3 decimal digits"
    )
    @Schema(
            description = "Cargo volume in cubic metres",
            example = "1.200",
            minimum = "0.001"
    )
    private BigDecimal volumeCbm;

    @Min(
            value = 1,
            message = "Cargo quantity must be at least 1"
    )
    @Schema(
            description = "Number of cargo units",
            example = "2",
            minimum = "1"
    )
    private Integer quantity;

    @Schema(
            description = "Indicates whether the cargo is fragile",
            example = "true"
    )
    private Boolean fragile;

    @Schema(
            description = "Indicates whether the cargo is hazardous",
            example = "false"
    )
    private Boolean hazardous;
}
