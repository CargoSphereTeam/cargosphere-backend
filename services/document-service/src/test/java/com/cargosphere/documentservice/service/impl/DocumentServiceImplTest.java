package com.cargosphere.documentservice.service.impl;

import com.cargosphere.documentservice.dto.DocumentResponse;
import com.cargosphere.documentservice.dto.UpdateVerificationRequest;
import com.cargosphere.documentservice.entity.Document;
import com.cargosphere.documentservice.entity.VerificationStatus;
import com.cargosphere.documentservice.exception.ResourceNotFoundException;
import com.cargosphere.documentservice.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    void getDocumentById_shouldReturnDocument_whenDocumentExists() {
        Document document = createDocument();

        when(documentRepository.findById(1L))
                .thenReturn(Optional.of(document));

        DocumentResponse response = documentService.getDocumentById(1L);

        assertNotNull(response);
        verify(documentRepository).findById(1L);
    }

    @Test
    void getDocumentById_shouldThrowException_whenDocumentDoesNotExist() {
        when(documentRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> documentService.getDocumentById(99L)
        );

        assertEquals("Document not found with ID: 99", exception.getMessage());
    }

    @Test
    void deleteDocument_shouldDeleteDocument_whenDocumentExists() {
        Document document = createDocument();

        when(documentRepository.findById(1L))
                .thenReturn(Optional.of(document));

        documentService.deleteDocument(1L);

        verify(documentRepository).findById(1L);
        verify(documentRepository).delete(document);
    }

    @Test
    void updateVerification_shouldRejectPendingStatus() {
        UpdateVerificationRequest request = new UpdateVerificationRequest();
        request.setVerificationStatus(VerificationStatus.PENDING);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> documentService.updateVerification(1L, request)
        );

        assertEquals(
                "Verification status must be VERIFIED or REJECTED",
                exception.getMessage()
        );

        verifyNoInteractions(documentRepository);
    }

    private Document createDocument() {
        Document document = new Document();
        document.setId(1L);
        document.setShipmentId(1001L);
        document.setDocumentType("INVOICE");
        document.setRequired(true);
        document.setVerificationStatus(VerificationStatus.PENDING);
        return document;
    }
}