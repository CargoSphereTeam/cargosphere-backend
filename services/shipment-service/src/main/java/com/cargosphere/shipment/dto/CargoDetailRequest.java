package com.cargosphere.shipment.dto;

import com.cargosphere.shipment.entity.enums.CargoType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoDetailRequest {

    @NotBlank(message = "Cargo name is required")
    @Size(max = 100, message = "Cargo name must not exceed 100 characters")
    private String cargoName;

    @Size(max = 255, message = "Cargo description must not exceed 255 characters")
    private String cargoDescription;

    private CargoType cargoType;

    @NotNull(message = "Cargo weight is required")
    @DecimalMin(value = "0.01", message = "Cargo weight must be greater than 0")
    private BigDecimal weightKg;

    @DecimalMin(value = "0.01", message = "Cargo volume must be greater than 0")
    private BigDecimal volumeCbm;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private Boolean fragile;

    private Boolean hazardous;
}