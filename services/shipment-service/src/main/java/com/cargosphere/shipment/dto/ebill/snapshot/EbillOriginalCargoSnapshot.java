package com.cargosphere.shipment.dto.ebill.snapshot;

import com.cargosphere.shipment.entity.enums.CargoType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EbillOriginalCargoSnapshot(
        Long cargoDetailId,
        String cargoName,
        String cargoDescription,
        CargoType cargoType,
        BigDecimal weightKg,
        BigDecimal volumeCbm,
        Integer quantity,
        Boolean fragile,
        Boolean hazardous,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
