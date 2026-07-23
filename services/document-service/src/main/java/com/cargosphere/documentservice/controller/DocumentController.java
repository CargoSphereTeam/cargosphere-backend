package com.cargosphere.documentservice.controller;

import com.cargosphere.documentservice.dto.CreateDocumentRequest;
import com.cargosphere.documentservice.dto.DocumentResponse;
import com.cargosphere.documentservice.dto.UpdateVerificationRequest;
import com.cargosphere.documentservice.service.DocumentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Validated
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "service", "document-service",
                "status", "UP"
        ));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @RequestBody CreateDocumentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.createDocument(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentResponse> getDocumentById(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @GetMapping("/shipment/{shipmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByShipmentId(
            @PathVariable @Positive Long shipmentId
    ) {
        return ResponseEntity.ok(
                documentService.getDocumentsByShipmentId(shipmentId)
        );
    }

    @PutMapping("/{id}/verification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentResponse> updateVerification(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateVerificationRequest request
    ) {
        return ResponseEntity.ok(
                documentService.updateVerification(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable @Positive Long id
    ) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
