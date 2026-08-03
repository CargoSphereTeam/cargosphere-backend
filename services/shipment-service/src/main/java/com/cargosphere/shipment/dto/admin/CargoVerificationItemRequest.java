package com.cargosphere.shipment.dto.admin;

import com.cargosphere.shipment.entity.enums.CargoType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
        name = "CargoVerificationItemRequest",
        description = "Confirmed cargo values entered by an administrator"
)
public class CargoVerificationItemRequest {

    @NotNull(message = "Cargo detail ID is required")
    @Positive(message = "Cargo detail ID must be greater than zero")
    @Schema(
            description = "ID of the original client-submitted cargo detail",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long cargoDetailId;

    @Size(
            max = 100,
            message = "Confirmed cargo name must not exceed 100 characters"
    )
    @Schema(
            description = "Administrator-confirmed cargo name",
            example = "Electronics Box",
            maxLength = 100
    )
    private String confirmedCargoName;

    @Size(
            max = 255,
            message = "Confirmed cargo description must not exceed 255 characters"
    )
    @Schema(
            description = "Administrator-confirmed cargo description",
            example = "Verified laptop accessories packed in protective boxes",
            maxLength = 255
    )
    private String confirmedCargoDescription;

    @Schema(
            description = "Administrator-confirmed cargo classification",
            example = "ELECTRONICS"
    )
    private CargoType confirmedCargoType;

    @DecimalMin(
            value = "0.001",
            message = "Confirmed cargo weight must be at least 0.001 kg"
    )
    @Digits(
            integer = 10,
            fraction = 3,
            message = "Confirmed cargo weight can contain up to 10 integer digits and 3 decimal digits"
    )
    @Schema(
            description = "Administrator-confirmed weight in kilograms",
            example = "25.500",
            minimum = "0.001"
    )
    private BigDecimal confirmedWeightKg;

    @DecimalMin(
            value = "0.001",
            message = "Confirmed cargo volume must be at least 0.001 CBM"
    )
    @Digits(
            integer = 10,
            fraction = 3,
            message = "Confirmed cargo volume can contain up to 10 integer digits and 3 decimal digits"
    )
    @Schema(
            description = "Administrator-confirmed volume in cubic metres",
            example = "1.200",
            minimum = "0.001"
    )
    private BigDecimal confirmedVolumeCbm;

    @Min(
            value = 1,
            message = "Confirmed cargo quantity must be at least 1"
    )
    @Schema(
            description = "Administrator-confirmed number of cargo units",
            example = "2",
            minimum = "1"
    )
    private Integer confirmedQuantity;

    @Schema(
            description = "Administrator-confirmed fragile indicator",
            example = "true"
    )
    private Boolean confirmedFragile;

    @Schema(
            description = "Administrator-confirmed hazardous indicator",
            example = "false"
    )
    private Boolean confirmedHazardous;

    @Size(
            max = 500,
            message = "Verification remarks must not exceed 500 characters"
    )
    @Schema(
            description = "Remarks recorded by the verifying administrator",
            example = "Physical cargo measurements verified",
            maxLength = 500
    )
    private String verificationRemarks;
}