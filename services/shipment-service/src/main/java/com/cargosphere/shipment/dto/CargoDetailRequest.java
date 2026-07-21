package com.cargosphere.shipment.dto;

import com.cargosphere.shipment.entity.enums.CargoType;
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
public class CargoDetailRequest {

    @NotBlank(message = "Cargo name is required")
    @Size(
            max = 100,
            message = "Cargo name must not exceed 100 characters"
    )
    private String cargoName;

    @Size(
            max = 255,
            message = "Cargo description must not exceed 255 characters"
    )
    private String cargoDescription;

    private CargoType cargoType;

    @NotNull(message = "Cargo weight is required")
    @DecimalMin(
            value = "0.001",
            message = "Cargo weight must be at least 0.001 kg"
    )
    @Digits(
            integer = 10,
            fraction = 3,
            message = "Cargo weight can contain up to 10 integer digits and 3 decimal digits"
    )
    private BigDecimal weightKg;

    @DecimalMin(
            value = "0.001",
            message = "Cargo volume must be at least 0.001 CBM"
    )
    @Digits(
            integer = 10,
            fraction = 3,
            message = "Cargo volume can contain up to 10 integer digits and 3 decimal digits"
    )
    private BigDecimal volumeCbm;

    @Min(
            value = 1,
            message = "Cargo quantity must be at least 1"
    )
    private Integer quantity;

    private Boolean fragile;

    private Boolean hazardous;
}