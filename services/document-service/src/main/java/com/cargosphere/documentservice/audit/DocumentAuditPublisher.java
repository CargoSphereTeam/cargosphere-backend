package com.cargosphere.documentservice.audit;

import com.cargosphere.documentservice.entity.Document;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentAuditPublisher {

    private final AuditClient auditClient;

    private final DocumentAuditEventFactory
            documentAuditEventFactory;

    private final DocumentActorProvider
            documentActorProvider;

    public void publishDocumentCreated(
            Document document
    ) {
        CurrentActor actor =
                documentActorProvider.getCurrentActor();

        AuditEventRequest event =
                documentAuditEventFactory
                        .documentCreated(
                                document,
                                actor
                        );

        auditClient.publish(event);
    }

    public void publishDocumentVerified(
            Document document
    ) {
        CurrentActor actor =
                documentActorProvider.getCurrentActor();

        AuditEventRequest event =
                documentAuditEventFactory
                        .documentVerified(
                                document,
                                actor
                        );

        auditClient.publish(event);
    }

    public void publishDocumentRejected(
            Document document
    ) {
        CurrentActor actor =
                documentActorProvider.getCurrentActor();

        AuditEventRequest event =
                documentAuditEventFactory
                        .documentRejected(
                                document,
                                actor
                        );

        auditClient.publish(event);
    }
}
