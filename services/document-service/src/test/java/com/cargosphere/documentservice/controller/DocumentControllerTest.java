package com.cargosphere.documentservice.controller;

import com.cargosphere.documentservice.config.SecurityConfig;
import com.cargosphere.documentservice.dto.CreateDocumentRequest;
import com.cargosphere.documentservice.dto.DocumentResponse;
import com.cargosphere.documentservice.dto.UpdateVerificationRequest;
import com.cargosphere.documentservice.entity.VerificationStatus;
import com.cargosphere.documentservice.exception.DuplicateDocumentException;
import com.cargosphere.documentservice.exception.GlobalExceptionHandler;
import com.cargosphere.documentservice.exception.ResourceNotFoundException;
import com.cargosphere.documentservice.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class
})
@WithMockUser(
        username = "admin@cargosphere.com",
        roles = "ADMIN"
)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @WithAnonymousUser
    void healthShouldRemainPublic() throws Exception {
        mockMvc.perform(get("/api/documents/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service")
                        .value("document-service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void createDocumentShouldReturnCreatedWhenRequestIsValid()
            throws Exception {

        when(documentService.createDocument(
                any(CreateDocumentRequest.class)
        )).thenReturn(response(
                1L,
                1001L,
                "COMMERCIAL_INVOICE"
        ));

        mockMvc.perform(
                        post("/api/documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "shipmentId": 1001,
                                          "documentType": "COMMERCIAL_INVOICE",
                                          "required": true,
                                          "remarks": "Required for customs"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.documentType")
                                .value("COMMERCIAL_INVOICE")
                );

        verify(documentService).createDocument(
                any(CreateDocumentRequest.class)
        );
    }

    @Test
    void createDocumentShouldReturnBadRequestWhenRequestIsEmpty()
            throws Exception {

        mockMvc.perform(
                        post("/api/documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.shipmentId"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.documentType"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.required"
                        ).exists()
                );

        verifyNoInteractions(documentService);
    }

    @Test
    void createDocumentShouldReturnBadRequestWhenShipmentIdIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/api/documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "documentType": "INVOICE",
                                          "required": true
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.shipmentId"
                        ).value("Shipment ID is required")
                );

        verifyNoInteractions(documentService);
    }

    @Test
    void createDocumentShouldReturnBadRequestWhenDocumentTypeIsBlank()
            throws Exception {

        mockMvc.perform(
                        post("/api/documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "shipmentId": 1001,
                                          "documentType": "   ",
                                          "required": true
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.documentType"
                        ).value("Document type is required")
                );

        verifyNoInteractions(documentService);
    }

    @Test
    void createDocumentShouldReturnBadRequestWhenRequiredFlagIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/api/documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "shipmentId": 1001,
                                          "documentType": "INVOICE"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.required"
                        ).value("Required flag is required")
                );

        verifyNoInteractions(documentService);
    }

    @Test
    void createDocumentShouldReturnBadRequestWhenDocumentTypeIsTooLong()
            throws Exception {

        String json = """
                {
                  "shipmentId": 1001,
                  "documentType": "%s",
                  "required": true
                }
                """.formatted("A".repeat(101));

        mockMvc.perform(
                        post("/api/documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.documentType"
                        ).value(
                                "Document type cannot exceed 100 characters"
                        )
                );

        verifyNoInteractions(documentService);
    }

    @Test
    void createDocumentShouldReturnConflictWhenDocumentIsDuplicate()
            throws Exception {

        when(documentService.createDocument(
                any(CreateDocumentRequest.class)
        )).thenThrow(
                new DuplicateDocumentException(
                        "Document type already exists for shipment: INVOICE"
                )
        );

        mockMvc.perform(
                        post("/api/documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "shipmentId": 1001,
                                          "documentType": "INVOICE",
                                          "required": true
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.message").value(
                                "Document type already exists for shipment: INVOICE"
                        )
                );
    }

    @Test
    void getAllDocumentsShouldReturnDocuments()
            throws Exception {

        when(documentService.getAllDocuments())
                .thenReturn(List.of(
                        response(1L, 1001L, "INVOICE"),
                        response(2L, 1002L, "PACKING_LIST")
                ));

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(
                        jsonPath("$[0].documentType")
                                .value("INVOICE")
                )
                .andExpect(
                        jsonPath("$[1].documentType")
                                .value("PACKING_LIST")
                );

        verify(documentService).getAllDocuments();
    }

    @Test
    void getDocumentByIdShouldReturnDocumentWhenDocumentExists()
            throws Exception {

        when(documentService.getDocumentById(1L))
                .thenReturn(response(
                        1L,
                        1001L,
                        "INVOICE"
                ));

        mockMvc.perform(get("/api/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.shipmentId").value(1001));

        verify(documentService).getDocumentById(1L);
    }

    @Test
    void getDocumentByIdShouldReturnNotFoundWhenDocumentDoesNotExist()
            throws Exception {

        when(documentService.getDocumentById(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Document not found with ID: 99"
                        )
                );

        mockMvc.perform(get("/api/documents/99"))
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value("Document not found with ID: 99")
                );

        verify(documentService).getDocumentById(99L);
    }

    @Test
    void getDocumentsByShipmentIdShouldReturnMatchingDocuments()
            throws Exception {

        when(documentService.getDocumentsByShipmentId(1001L))
                .thenReturn(List.of(
                        response(1L, 1001L, "INVOICE"),
                        response(2L, 1001L, "PACKING_LIST")
                ));

        mockMvc.perform(
                        get("/api/documents/shipment/1001")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(
                        jsonPath("$[0].shipmentId")
                                .value(1001)
                )
                .andExpect(
                        jsonPath("$[1].shipmentId")
                                .value(1001)
                );

        verify(documentService)
                .getDocumentsByShipmentId(1001L);
    }

    @Test
    void updateVerificationShouldReturnUpdatedDocumentWhenRequestIsValid()
            throws Exception {

        DocumentResponse updated = DocumentResponse.builder()
                .id(1L)
                .shipmentId(1001L)
                .documentType("INVOICE")
                .required(true)
                .verificationStatus(
                        VerificationStatus.VERIFIED
                )
                .verifiedBy(10L)
                .verifiedAt(LocalDateTime.now())
                .remarks("Checked and approved")
                .build();

        when(documentService.updateVerification(
                eq(1L),
                any(UpdateVerificationRequest.class)
        )).thenReturn(updated);

        mockMvc.perform(
                        put("/api/documents/1/verification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "verificationStatus": "VERIFIED",
                                          "verifiedBy": 10,
                                          "remarks": "Checked and approved"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.verificationStatus")
                                .value("VERIFIED")
                )
                .andExpect(jsonPath("$.verifiedBy").value(10))
                .andExpect(jsonPath("$.verifiedAt").exists());

        verify(documentService).updateVerification(
                eq(1L),
                any(UpdateVerificationRequest.class)
        );
    }

    @Test
    void updateVerificationShouldReturnBadRequestWhenRequiredFieldsAreMissing()
            throws Exception {

        mockMvc.perform(
                        put("/api/documents/1/verification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.verificationStatus"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.verifiedBy"
                        ).exists()
                );

        verifyNoInteractions(documentService);
    }

    @Test
    void updateVerificationShouldReturnBadRequestWhenStatusIsPending()
            throws Exception {

        when(documentService.updateVerification(
                eq(1L),
                any(UpdateVerificationRequest.class)
        )).thenThrow(
                new IllegalArgumentException(
                        "Verification status must be VERIFIED or REJECTED"
                )
        );

        mockMvc.perform(
                        put("/api/documents/1/verification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "verificationStatus": "PENDING",
                                          "verifiedBy": 10
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message").value(
                                "Verification status must be VERIFIED or REJECTED"
                        )
                );
    }

    @Test
    void updateVerificationShouldReturnNotFoundWhenDocumentDoesNotExist()
            throws Exception {

        when(documentService.updateVerification(
                eq(99L),
                any(UpdateVerificationRequest.class)
        )).thenThrow(
                new ResourceNotFoundException(
                        "Document not found with ID: 99"
                )
        );

        mockMvc.perform(
                        put("/api/documents/99/verification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "verificationStatus": "VERIFIED",
                                          "verifiedBy": 10
                                        }
                                        """)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value("Document not found with ID: 99")
                );
    }

    @Test
    void deleteDocumentShouldReturnNoContent()
            throws Exception {

        doNothing()
                .when(documentService)
                .deleteDocument(1L);

        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isNoContent());

        verify(documentService).deleteDocument(1L);
    }

    @Test
    void deleteDocumentShouldReturnNotFoundWhenDocumentDoesNotExist()
            throws Exception {

        doThrow(
                new ResourceNotFoundException(
                        "Document not found with ID: 99"
                )
        ).when(documentService).deleteDocument(99L);

        mockMvc.perform(delete("/api/documents/99"))
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value("Document not found with ID: 99")
                );

        verify(documentService).deleteDocument(99L);
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void clientShouldCreateDocument() throws Exception {
        when(documentService.createDocument(
                any(CreateDocumentRequest.class)
        )).thenReturn(response(
                1L,
                1001L,
                "INVOICE"
        ));

        mockMvc.perform(
                        post("/api/documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "shipmentId": 1001,
                                          "documentType": "INVOICE",
                                          "required": true
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        verify(documentService).createDocument(
                any(CreateDocumentRequest.class)
        );
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void clientShouldNotGetAllDocuments() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(
                        jsonPath("$.message")
                                .value("Access denied")
                );

        verifyNoInteractions(documentService);
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void clientShouldNotUpdateDocumentVerification()
            throws Exception {

        mockMvc.perform(
                        put("/api/documents/1/verification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "verificationStatus": "VERIFIED",
                                          "verifiedBy": 10
                                        }
                                        """)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(
                        jsonPath("$.message")
                                .value("Access denied")
                );

        verifyNoInteractions(documentService);
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void clientShouldNotDeleteDocument() throws Exception {
        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(
                        jsonPath("$.message")
                                .value("Access denied")
                );

        verifyNoInteractions(documentService);
    }

    @Test
    @WithAnonymousUser
    void unauthenticatedUserShouldNotAccessProtectedEndpoint()
            throws Exception {

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(documentService);
    }

    private DocumentResponse response(
            Long id,
            Long shipmentId,
            String documentType
    ) {
        return DocumentResponse.builder()
                .id(id)
                .shipmentId(shipmentId)
                .documentType(documentType)
                .required(true)
                .verificationStatus(
                        VerificationStatus.PENDING
                )
                .remarks("Required for customs")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}