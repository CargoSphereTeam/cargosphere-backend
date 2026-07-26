package com.cargosphere.documentservice.audit;

import com.cargosphere.documentservice.entity.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentAuditEventFactoryTest {

    private DocumentAuditEventFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DocumentAuditEventFactory();
    }

    @Test
    void documentCreatedShouldCreateSuccessEvent() {
        Document document = document();

        AuditEventRequest event =
                factory.documentCreated(
                        document,
                        new CurrentActor(
                                10L,
                                "ROLE_CLIENT"
                        )
                );

        assertEquals(
                "DOCUMENT_CREATED",
                event.action()
        );

        assertEquals(
                "DOCUMENT",
                event.entityType()
        );

        assertEquals(
                "55",
                event.entityId()
        );

        assertEquals(
                "document-service",
                event.serviceName()
        );

        assertEquals(
                "SUCCESS",
                event.outcome()
        );

        assertEquals(
                "POST",
                event.httpMethod()
        );

        assertEquals(
                "/api/documents",
                event.endpoint()
        );

        assertEquals(
                201,
                event.statusCode()
        );
    }

    @Test
    void documentVerifiedShouldCreateVerifiedEvent() {
        Document document = document();

        AuditEventRequest event =
                factory.documentVerified(
                        document,
                        new CurrentActor(
                                1L,
                                "ROLE_ADMIN"
                        )
                );

        assertEquals(
                "DOCUMENT_VERIFIED",
                event.action()
        );

        assertEquals(
                1L,
                event.actorUserId()
        );

        assertEquals(
                "ROLE_ADMIN",
                event.actorRole()
        );

        assertEquals(
                "PUT",
                event.httpMethod()
        );

        assertEquals(
                "/api/documents/55/verification",
                event.endpoint()
        );

        assertEquals(
                200,
                event.statusCode()
        );
    }

    @Test
    void documentRejectedShouldCreateRejectedEvent() {
        AuditEventRequest event =
                factory.documentRejected(
                        document(),
                        null
                );

        assertEquals(
                "DOCUMENT_REJECTED",
                event.action()
        );

        assertEquals(
                "Document rejected successfully",
                event.description()
        );

        assertEquals(
                null,
                event.actorUserId()
        );

        assertEquals(
                null,
                event.actorRole()
        );
    }

    private Document document() {
        return Document.builder()
                .id(55L)
                .shipmentId(1001L)
                .documentType("INVOICE")
                .required(true)
                .build();
    }
}
