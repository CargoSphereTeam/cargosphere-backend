package com.cargosphere.documentservice.service.impl;

import com.cargosphere.documentservice.audit.CurrentActor;
import com.cargosphere.documentservice.audit.DocumentActorProvider;
import com.cargosphere.documentservice.audit.DocumentAuditPublisher;
import com.cargosphere.documentservice.dto.CreateDocumentRequest;
import com.cargosphere.documentservice.dto.DocumentResponse;
import com.cargosphere.documentservice.dto.UpdateVerificationRequest;
import com.cargosphere.documentservice.entity.Document;
import com.cargosphere.documentservice.entity.VerificationStatus;
import com.cargosphere.documentservice.exception.DuplicateDocumentException;
import com.cargosphere.documentservice.exception.ResourceNotFoundException;
import com.cargosphere.documentservice.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentActorProvider documentActorProvider;

    @Mock
    private DocumentAuditPublisher documentAuditPublisher;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @BeforeEach
    void setUpCurrentActor() {
        documentService.setDocumentActorProvider(documentActorProvider);
        documentService.setDocumentAuditPublisher(documentAuditPublisher);

        lenient()
                .when(documentActorProvider.getCurrentActor())
                .thenReturn(
                        new CurrentActor(
                                10L,
                                "ROLE_ADMIN"
                        )
                );
    }

    @Test
    void createDocument_shouldSaveNormalizedDocument() {
        CreateDocumentRequest request =
                createRequest(" commercial_invoice ");

        when(documentRepository.existsByShipmentIdAndDocumentType(
                1001L, "COMMERCIAL_INVOICE"
        )).thenReturn(false);

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> {
                    Document saved = invocation.getArgument(0);
                    saved.setId(1L);
                    saved.setCreatedAt(LocalDateTime.now());
                    saved.setUpdatedAt(LocalDateTime.now());
                    return saved;
                });

        DocumentResponse response =
                documentService.createDocument(request);

        assertAll(
                () -> assertEquals(1L, response.getId()),
                () -> assertEquals(1001L, response.getShipmentId()),
                () -> assertEquals(
                        "COMMERCIAL_INVOICE",
                        response.getDocumentType()
                ),
                () -> assertTrue(response.getRequired()),
                () -> assertEquals(
                        VerificationStatus.PENDING,
                        response.getVerificationStatus()
                ),
                () -> assertEquals(
                        "Required for customs",
                        response.getRemarks()
                )
        );

        ArgumentCaptor<Document> captor =
                ArgumentCaptor.forClass(Document.class);

        verify(documentRepository).save(captor.capture());

        assertEquals(
                "COMMERCIAL_INVOICE",
                captor.getValue().getDocumentType()
        );
    }

    @Test
    void createDocument_shouldThrowConflict_whenDocumentAlreadyExists() {
        CreateDocumentRequest request = createRequest("invoice");

        when(documentRepository.existsByShipmentIdAndDocumentType(
                1001L, "INVOICE"
        )).thenReturn(true);

        DuplicateDocumentException exception = assertThrows(
                DuplicateDocumentException.class,
                () -> documentService.createDocument(request)
        );

        assertEquals(
                "Document type already exists for shipment: INVOICE",
                exception.getMessage()
        );

        verify(documentRepository, never()).save(any());
    }

    @Test
    void getAllDocuments_shouldReturnMappedDocuments() {
        when(documentRepository.findAll()).thenReturn(List.of(
                createDocument(1L, 1001L, "INVOICE"),
                createDocument(2L, 1002L, "PACKING_LIST")
        ));

        List<DocumentResponse> responses =
                documentService.getAllDocuments();

        assertEquals(2, responses.size());
        assertEquals("INVOICE", responses.get(0).getDocumentType());
        assertEquals(
                "PACKING_LIST",
                responses.get(1).getDocumentType()
        );

        verify(documentRepository).findAll();
    }

    @Test
    void getAllDocuments_shouldReturnEmptyList() {
        when(documentRepository.findAll()).thenReturn(List.of());

        List<DocumentResponse> responses =
                documentService.getAllDocuments();

        assertTrue(responses.isEmpty());
    }

    @Test
    void getDocumentById_shouldReturnDocument() {
        Document document =
                createDocument(1L, 1001L, "INVOICE");

        when(documentRepository.findById(1L))
                .thenReturn(Optional.of(document));

        DocumentResponse response =
                documentService.getDocumentById(1L);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(1L, response.getId()),
                () -> assertEquals(1001L, response.getShipmentId()),
                () -> assertEquals(
                        "INVOICE",
                        response.getDocumentType()
                )
        );
    }

    @Test
    void getDocumentById_shouldThrowException_whenNotFound() {
        when(documentRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> documentService.getDocumentById(99L)
        );

        assertEquals(
                "Document not found with ID: 99",
                exception.getMessage()
        );
    }

    @Test
    void getDocumentsByShipmentId_shouldReturnDocuments() {
        when(documentRepository.findByShipmentId(1001L))
                .thenReturn(List.of(
                        createDocument(1L, 1001L, "INVOICE"),
                        createDocument(2L, 1001L, "PACKING_LIST")
                ));

        List<DocumentResponse> responses =
                documentService.getDocumentsByShipmentId(1001L);

        assertEquals(2, responses.size());

        assertTrue(responses.stream().allMatch(
                response -> response.getShipmentId().equals(1001L)
        ));
    }

    @Test
    void getDocumentsByShipmentId_shouldReturnEmptyList() {
        when(documentRepository.findByShipmentId(7777L))
                .thenReturn(List.of());

        List<DocumentResponse> responses =
                documentService.getDocumentsByShipmentId(7777L);

        assertTrue(responses.isEmpty());
    }

    @Test
    void updateVerification_shouldMarkDocumentVerified() {
        Document document =
                createDocument(1L, 1001L, "INVOICE");

        UpdateVerificationRequest request =
                verificationRequest(
                        VerificationStatus.VERIFIED,
                        "Checked and approved"
                );

        when(documentRepository.findById(1L))
                .thenReturn(Optional.of(document));

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponse response =
                documentService.updateVerification(1L, request);

        assertAll(
                () -> assertEquals(
                        VerificationStatus.VERIFIED,
                        response.getVerificationStatus()
                ),
                () -> assertEquals(10L, response.getVerifiedBy()),
                () -> assertNotNull(response.getVerifiedAt()),
                () -> assertEquals(
                        "Checked and approved",
                        response.getRemarks()
                )
        );
    }

    @Test
    void updateVerification_shouldMarkDocumentRejected() {
        Document document =
                createDocument(1L, 1001L, "INVOICE");

        UpdateVerificationRequest request =
                verificationRequest(
                        VerificationStatus.REJECTED,
                        "Invalid document"
                );

        when(documentRepository.findById(1L))
                .thenReturn(Optional.of(document));

        when(documentRepository.save(document))
                .thenReturn(document);

        DocumentResponse response =
                documentService.updateVerification(1L, request);

        assertEquals(
                VerificationStatus.REJECTED,
                response.getVerificationStatus()
        );

        assertEquals(
                "Invalid document",
                response.getRemarks()
        );
    }

    @Test
    void updateVerification_shouldThrowNotFound() {
        UpdateVerificationRequest request =
                verificationRequest(
                        VerificationStatus.VERIFIED,
                        "Approved"
                );

        when(documentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> documentService.updateVerification(99L, request)
        );

        verify(documentRepository, never()).save(any());
    }

@Test
void updateVerification_shouldMarkDocumentPending() {
    Document document =
            createDocument(1L, 1001L, "INVOICE");

    document.setVerificationStatus(
            VerificationStatus.VERIFIED
    );
    document.setVerifiedBy(10L);
    document.setVerifiedAt(LocalDateTime.now());

    UpdateVerificationRequest request =
            verificationRequest(
                    VerificationStatus.PENDING,
                    "Waiting for resubmission"
            );

    when(documentRepository.findById(1L))
            .thenReturn(Optional.of(document));

    when(documentRepository.save(document))
            .thenReturn(document);

    DocumentResponse response =
            documentService.updateVerification(
                    1L,
                    request
            );

    assertAll(
            () -> assertEquals(
                    VerificationStatus.PENDING,
                    response.getVerificationStatus()
            ),
            () -> assertNull(
                    response.getVerifiedBy()
            ),
            () -> assertNull(
                    response.getVerifiedAt()
            ),
            () -> assertEquals(
                    "Waiting for resubmission",
                    response.getRemarks()
            )
    );

    verify(documentRepository).findById(1L);
    verify(documentRepository).save(document);
}

    @Test
    void deleteDocument_shouldDeleteExistingDocument() {
        Document document =
                createDocument(1L, 1001L, "INVOICE");

        when(documentRepository.findById(1L))
                .thenReturn(Optional.of(document));

        documentService.deleteDocument(1L);

        verify(documentRepository).delete(document);
    }

    @Test
    void deleteDocument_shouldThrowNotFound() {
        when(documentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> documentService.deleteDocument(99L)
        );

        verify(documentRepository, never()).delete(any());
    }

    private CreateDocumentRequest createRequest(String documentType) {
        CreateDocumentRequest request =
                new CreateDocumentRequest();

        request.setShipmentId(1001L);
        request.setDocumentType(documentType);
        request.setRequired(true);
        request.setRemarks("Required for customs");

        return request;
    }

    private UpdateVerificationRequest verificationRequest(
            VerificationStatus status,
            String remarks
    ) {
        UpdateVerificationRequest request =
                new UpdateVerificationRequest();

        request.setVerificationStatus(status);
        request.setRemarks(remarks);

        return request;
    }

    private Document createDocument(
            Long id,
            Long shipmentId,
            String documentType
    ) {
        return Document.builder()
                .id(id)
                .shipmentId(shipmentId)
                .documentType(documentType)
                .required(true)
                .verificationStatus(VerificationStatus.PENDING)
                .remarks("Required for customs")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
