package com.cargosphere.shipment.dto.admin;

import com.cargosphere.shipment.entity.enums.CargoType;
import com.cargosphere.shipment.entity.enums.CargoVerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "CargoVerificationItemResponse",
        description = "Stored verification details for one cargo item"
)
public class CargoVerificationItemResponse {

    @Schema(description = "Cargo verification record identifier", example = "15")
    private Long id;

    @Schema(description = "Original cargo detail identifier", example = "1")
    private Long cargoDetailId;

    @Schema(description = "Current cargo verification status", example = "DRAFT")
    private CargoVerificationStatus verificationStatus;

    private String confirmedCargoName;

    private String confirmedCargoDescription;

    private CargoType confirmedCargoType;

    private BigDecimal confirmedWeightKg;

    private BigDecimal confirmedVolumeCbm;

    private Integer confirmedQuantity;

    private Boolean confirmedFragile;

    private Boolean confirmedHazardous;

    private String verificationRemarks;

    @Schema(description = "Administrator user ID who confirmed the cargo", example = "5")
    private Long verifiedBy;

    private LocalDateTime verifiedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}