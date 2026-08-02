package com.cargosphere.shipment.dto.ebill.snapshot;

import com.cargosphere.shipment.entity.enums.CargoType;
import com.cargosphere.shipment.entity.enums.CargoVerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EbillConfirmedCargoSnapshot(
        Long verificationId,
        Long cargoDetailId,
        CargoVerificationStatus verificationStatus,
        String confirmedCargoName,
        String confirmedCargoDescription,
        CargoType confirmedCargoType,
        BigDecimal confirmedWeightKg,
        BigDecimal confirmedVolumeCbm,
        Integer confirmedQuantity,
        Boolean confirmedFragile,
        Boolean confirmedHazardous,
        String verificationRemarks,
        Long verifiedBy,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
