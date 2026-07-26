package com.cargosphere.documentservice.dto;

import com.cargosphere.documentservice.entity.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateVerificationRequest {

    @NotNull(message = "Verification status is required")
    private VerificationStatus verificationStatus;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
}
