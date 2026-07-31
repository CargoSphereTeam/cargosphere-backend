package com.cargosphere.documentservice.controller;

import com.cargosphere.documentservice.config.OpenApiConfig;
import com.cargosphere.documentservice.dto.CreateDocumentRequest;
import com.cargosphere.documentservice.dto.DocumentResponse;
import com.cargosphere.documentservice.dto.ErrorResponse;
import com.cargosphere.documentservice.dto.UpdateVerificationRequest;
import com.cargosphere.documentservice.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Documents",
        description =
                "Shipment document checklist and verification management"
)
public class DocumentController {

    private final DocumentService documentService;

    @Operation(
            summary = "Check document-service health",
            description =
                    "Returns the current document-service availability."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Document-service is running"
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "service", "document-service",
                        "status", "UP"
                )
        );
    }

    @Operation(
            summary = "Create a shipment document",
            description =
                    "Creates a document checklist entry for a shipment. "
                            + "Accessible to ROLE_ADMIN and ROLE_CLIENT."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Document created successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = DocumentResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN or CLIENT role is required"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description =
                            "The document type already exists "
                                    + "for the shipment",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @RequestBody CreateDocumentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documentService.createDocument(request));
    }

    @Operation(
            summary = "Get all documents",
            description =
                    "Returns every document checklist entry. "
                            + "Requires ROLE_ADMIN."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Documents returned successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation =
                                                    DocumentResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        return ResponseEntity.ok(
                documentService.getAllDocuments()
        );
    }

    @Operation(
            summary = "Get document by ID",
            description =
                    "Returns one document checklist entry. "
                            + "Requires ROLE_ADMIN."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Document returned successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = DocumentResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Document ID must be positive",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Document was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentResponse> getDocumentById(
            @Parameter(
                    description = "Document database ID",
                    example = "1",
                    required = true
            )
            @PathVariable
            @Positive
            Long id
    ) {
        return ResponseEntity.ok(
                documentService.getDocumentById(id)
        );
    }

    @Operation(
            summary = "Get documents by shipment ID",
            description =
                    "Returns all document checklist entries for a shipment. "
                            + "Requires ROLE_ADMIN."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipment documents returned successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation =
                                                    DocumentResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Shipment ID must be positive",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            )
    })
    @GetMapping("/shipment/{shipmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentResponse>>
    getDocumentsByShipmentId(
            @Parameter(
                    description = "Shipment database ID",
                    example = "1001",
                    required = true
            )
            @PathVariable
            @Positive
            Long shipmentId
    ) {
        return ResponseEntity.ok(
                documentService.getDocumentsByShipmentId(shipmentId)
        );
    }

    @Operation(
            summary = "Update document verification",
            description =
                    "Marks a document as PENDING, VERIFIED or REJECTED. "
                            + "Requires ROLE_ADMIN."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description =
                            "Document verification updated successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = DocumentResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Request validation failed or ID is invalid",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Document was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PutMapping("/{id}/verification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentResponse> updateVerification(
            @Parameter(
                    description = "Document database ID",
                    example = "1",
                    required = true
            )
            @PathVariable
            @Positive
            Long id,

            @Valid
            @RequestBody
            UpdateVerificationRequest request
    ) {
        return ResponseEntity.ok(
                documentService.updateVerification(id, request)
        );
    }

    @Operation(
            summary = "Delete a document",
            description =
                    "Deletes a document checklist entry. "
                            + "Requires ROLE_ADMIN."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Document deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Document ID must be positive",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Document was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDocument(
            @Parameter(
                    description = "Document database ID",
                    example = "1",
                    required = true
            )
            @PathVariable
            @Positive
            Long id
    ) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
