package com.cargosphere.shipment.dto;

import com.cargosphere.shipment.entity.enums.CargoType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoDetailResponse {

    private Long id;

    private Long shipmentId;

    private String cargoName;

    private String cargoDescription;

    private CargoType cargoType;

    private BigDecimal weightKg;

    private BigDecimal volumeCbm;

    private Integer quantity;

    private Boolean fragile;

    private Boolean hazardous;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}