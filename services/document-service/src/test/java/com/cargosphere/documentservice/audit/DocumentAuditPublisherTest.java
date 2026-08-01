package com.cargosphere.documentservice.audit;

import com.cargosphere.documentservice.entity.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentAuditPublisherTest {

    @Mock
    private AuditClient auditClient;

    @Mock
    private DocumentAuditEventFactory
            documentAuditEventFactory;

    @Mock
    private DocumentActorProvider
            documentActorProvider;

    @InjectMocks
    private DocumentAuditPublisher
            documentAuditPublisher;

    @Test
    void publishDocumentCreatedShouldPublishEvent() {
        Document document = document();

        CurrentActor actor =
                new CurrentActor(
                        10L,
                        "ROLE_CLIENT"
                );

        AuditEventRequest event =
                event("DOCUMENT_CREATED");

        when(documentActorProvider
                .getCurrentActor())
                .thenReturn(actor);

        when(documentAuditEventFactory
                .documentCreated(
                        document,
                        actor
                ))
                .thenReturn(event);

        documentAuditPublisher
                .publishDocumentCreated(document);

        verify(auditClient)
                .publish(event);
    }

    @Test
    void publishDocumentVerifiedShouldPublishEvent() {
        Document document = document();

        CurrentActor actor =
                new CurrentActor(
                        1L,
                        "ROLE_ADMIN"
                );

        AuditEventRequest event =
                event("DOCUMENT_VERIFIED");

        when(documentActorProvider
                .getCurrentActor())
                .thenReturn(actor);

        when(documentAuditEventFactory
                .documentVerified(
                        document,
                        actor
                ))
                .thenReturn(event);

        documentAuditPublisher
                .publishDocumentVerified(document);

        verify(auditClient)
                .publish(event);
    }

    @Test
    void publishDocumentRejectedShouldPublishEvent() {
        Document document = document();

        CurrentActor actor =
                new CurrentActor(
                        1L,
                        "ROLE_ADMIN"
                );

        AuditEventRequest event =
                event("DOCUMENT_REJECTED");

        when(documentActorProvider
                .getCurrentActor())
                .thenReturn(actor);

        when(documentAuditEventFactory
                .documentRejected(
                        document,
                        actor
                ))
                .thenReturn(event);

        documentAuditPublisher
                .publishDocumentRejected(document);

        verify(auditClient)
                .publish(event);
    }

    private Document document() {
        return Document.builder()
                .id(55L)
                .shipmentId(1001L)
                .documentType("INVOICE")
                .required(true)
                .build();
    }

    private AuditEventRequest event(
            String action
    ) {
        return new AuditEventRequest(
                10L,
                "ROLE_CLIENT",
                action,
                "DOCUMENT",
                "55",
                "document-service",
                "Document audit event",
                "SUCCESS",
                null,
                null,
                "POST",
                "/api/documents",
                201
        );
    }
}
