package com.cargosphere.documentservice.repository;

import com.cargosphere.documentservice.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByShipmentId(Long shipmentId);

    Optional<Document> findByShipmentIdAndDocumentType(
            Long shipmentId,
            String documentType
    );

    boolean existsByShipmentIdAndDocumentType(
            Long shipmentId,
            String documentType
    );
}