package com.cargosphere.documentservice.mapper;

import com.cargosphere.documentservice.dto.CreateDocumentRequest;
import com.cargosphere.documentservice.dto.DocumentResponse;
import com.cargosphere.documentservice.entity.Document;
import com.cargosphere.documentservice.entity.VerificationStatus;

import java.util.Locale;

public final class DocumentMapper {

    private DocumentMapper() {
    }

    public static Document toEntity(CreateDocumentRequest request) {
        return Document.builder()
                .shipmentId(request.getShipmentId())
                .documentType(
                        request.getDocumentType()
                                .trim()
                                .toUpperCase(Locale.ROOT)
                )
                .required(request.getRequired())
                .verificationStatus(VerificationStatus.PENDING)
                .remarks(request.getRemarks())
                .build();
    }

    public static DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .shipmentId(document.getShipmentId())
                .documentType(document.getDocumentType())
                .required(document.getRequired())
                .verificationStatus(document.getVerificationStatus())
                .verifiedBy(document.getVerifiedBy())
                .verifiedAt(document.getVerifiedAt())
                .remarks(document.getRemarks())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}