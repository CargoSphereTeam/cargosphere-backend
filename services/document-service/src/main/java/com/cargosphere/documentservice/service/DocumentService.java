package com.cargosphere.documentservice.service;

import com.cargosphere.documentservice.dto.CreateDocumentRequest;
import com.cargosphere.documentservice.dto.DocumentResponse;
import com.cargosphere.documentservice.dto.UpdateVerificationRequest;

import java.util.List;

public interface DocumentService {

    DocumentResponse createDocument(CreateDocumentRequest request);

    List<DocumentResponse> getAllDocuments();

    DocumentResponse getDocumentById(Long id);

    List<DocumentResponse> getDocumentsByShipmentId(Long shipmentId);

    DocumentResponse updateVerification(
            Long id,
            UpdateVerificationRequest request
    );

    void deleteDocument(Long id);
}