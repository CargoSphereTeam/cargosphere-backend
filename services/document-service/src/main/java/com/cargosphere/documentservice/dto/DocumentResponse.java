package com.cargosphere.documentservice.dto;

import com.cargosphere.documentservice.entity.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private Long shipmentId;
    private String documentType;
    private Boolean required;
    private VerificationStatus verificationStatus;
    private Long verifiedBy;
    private LocalDateTime verifiedAt;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}