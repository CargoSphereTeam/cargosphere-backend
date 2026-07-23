package com.cargosphere.documentservice.repository;

import com.cargosphere.documentservice.entity.Document;
import com.cargosphere.documentservice.entity.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    @BeforeEach
    void cleanDatabase() {
        documentRepository.deleteAll();
    }

    @Test
    void saveAndFindById_shouldPersistDocument() {
        Document saved = documentRepository.saveAndFlush(
                document(990001L, "COMMERCIAL_INVOICE")
        );

        Optional<Document> result =
                documentRepository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals(990001L, result.get().getShipmentId());
        assertEquals(
                "COMMERCIAL_INVOICE",
                result.get().getDocumentType()
        );
        assertNotNull(result.get().getCreatedAt());
    }

    @Test
    void findByShipmentId_shouldReturnOnlyMatchingDocuments() {
        documentRepository.save(
                document(990001L, "COMMERCIAL_INVOICE")
        );
        documentRepository.save(
                document(990001L, "PACKING_LIST")
        );
        documentRepository.save(
                document(990002L, "BILL_OF_LADING")
        );
        documentRepository.flush();

        List<Document> results =
                documentRepository.findByShipmentId(990001L);

        assertEquals(2, results.size());

        assertTrue(results.stream().allMatch(
                document ->
                        document.getShipmentId().equals(990001L)
        ));
    }

    @Test
    void findByShipmentIdAndDocumentType_shouldReturnMatchingDocument() {
        documentRepository.saveAndFlush(
                document(990001L, "PACKING_LIST")
        );

        Optional<Document> result =
                documentRepository.findByShipmentIdAndDocumentType(
                        990001L,
                        "PACKING_LIST"
                );

        assertTrue(result.isPresent());
        assertEquals(
                "PACKING_LIST",
                result.get().getDocumentType()
        );
    }

    @Test
    void existsByShipmentIdAndDocumentType_shouldReportDuplicate() {
        documentRepository.saveAndFlush(
                document(990001L, "INVOICE")
        );

        boolean exists =
                documentRepository.existsByShipmentIdAndDocumentType(
                        990001L,
                        "INVOICE"
                );

        assertTrue(exists);

        assertFalse(
                documentRepository.existsByShipmentIdAndDocumentType(
                        990001L,
                        "PACKING_LIST"
                )
        );
    }

    @Test
    void uniqueConstraint_shouldRejectDuplicateShipmentAndDocumentType() {
        documentRepository.saveAndFlush(
                document(990001L, "INVOICE")
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> documentRepository.saveAndFlush(
                        document(990001L, "INVOICE")
                )
        );
    }

    @Test
    void delete_shouldRemoveDocument() {
        Document saved = documentRepository.saveAndFlush(
                document(990001L, "INVOICE")
        );

        documentRepository.delete(saved);
        documentRepository.flush();

        assertFalse(
                documentRepository.findById(saved.getId()).isPresent()
        );
    }

    private Document document(
            Long shipmentId,
            String documentType
    ) {
        return Document.builder()
                .shipmentId(shipmentId)
                .documentType(documentType)
                .required(true)
                .verificationStatus(VerificationStatus.PENDING)
                .remarks("Automated repository test")
                .build();
    }
}