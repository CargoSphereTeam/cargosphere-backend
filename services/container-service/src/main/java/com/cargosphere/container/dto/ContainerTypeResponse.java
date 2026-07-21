package com.cargosphere.container.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContainerTypeResponse(
        Long containerTypeId,
        String typeCode,
        String typeName,
        String description,
        BigDecimal maxWeightKg,
        BigDecimal maxVolumeCbm,
        BigDecimal lengthM,
        BigDecimal widthM,
        BigDecimal heightM,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}