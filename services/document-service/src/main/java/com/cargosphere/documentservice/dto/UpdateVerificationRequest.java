package com.cargosphere.documentservice.dto;

import com.cargosphere.documentservice.entity.VerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "UpdateVerificationRequest",
        description =
                "Request body used to update document verification"
)
public class UpdateVerificationRequest {

    @NotNull(message = "Verification status is required")
    @Schema(
            description = "New document verification status",
            example = "VERIFIED",
            allowableValues = {
                    "PENDING",
                    "SUBMITTED",
                    "VERIFIED",
                    "REJECTED",
                    "NOT_APPLICABLE"
            },
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private VerificationStatus verificationStatus;

    @Size(
            max = 500,
            message = "Remarks cannot exceed 500 characters"
    )
    @Schema(
            description = "Optional verification remarks",
            example = "Document checked and approved",
            maxLength = 500
    )
    private String remarks;
}