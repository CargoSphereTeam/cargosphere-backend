package com.cargosphere.documentservice.service.impl;

import com.cargosphere.documentservice.dto.CreateDocumentRequest;
import com.cargosphere.documentservice.dto.DocumentResponse;
import com.cargosphere.documentservice.dto.UpdateVerificationRequest;
import com.cargosphere.documentservice.entity.Document;
import com.cargosphere.documentservice.entity.VerificationStatus;
import com.cargosphere.documentservice.exception.DuplicateDocumentException;
import com.cargosphere.documentservice.exception.ResourceNotFoundException;
import com.cargosphere.documentservice.mapper.DocumentMapper;
import com.cargosphere.documentservice.repository.DocumentRepository;
import com.cargosphere.documentservice.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;

    @Override
    public DocumentResponse createDocument(CreateDocumentRequest request) {
        String normalizedType = request.getDocumentType()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (documentRepository.existsByShipmentIdAndDocumentType(
                request.getShipmentId(),
                normalizedType
        )) {
            throw new DuplicateDocumentException(
                    "Document type already exists for shipment: " + normalizedType
            );
        }

        Document document = DocumentMapper.toEntity(request);
        Document savedDocument = documentRepository.save(document);

        return DocumentMapper.toResponse(savedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(DocumentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id) {
        return DocumentMapper.toResponse(findDocument(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByShipmentId(Long shipmentId) {
        return documentRepository.findByShipmentId(shipmentId)
                .stream()
                .map(DocumentMapper::toResponse)
                .toList();
    }

    @Override
    public DocumentResponse updateVerification(
            Long id,
            UpdateVerificationRequest request
    ) {
        if (request.getVerificationStatus() == VerificationStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Verification status must be VERIFIED or REJECTED"
            );
        }

        Document document = findDocument(id);
        document.setVerificationStatus(request.getVerificationStatus());
        document.setVerifiedBy(request.getVerifiedBy());
        document.setVerifiedAt(LocalDateTime.now());
        document.setRemarks(request.getRemarks());

        Document updatedDocument = documentRepository.save(document);
        return DocumentMapper.toResponse(updatedDocument);
    }

    @Override
    public void deleteDocument(Long id) {
        Document document = findDocument(id);
        documentRepository.delete(document);
    }

    private Document findDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found with ID: " + id
                ));
    }
}