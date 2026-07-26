package com.cargosphere.documentservice.audit;

import com.cargosphere.documentservice.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentAuditEventFactory {

    private static final String SERVICE_NAME =
            "document-service";

    private static final String ENTITY_TYPE =
            "DOCUMENT";

    private static final String SUCCESS =
            "SUCCESS";

    public AuditEventRequest documentCreated(
            Document document,
            CurrentActor actor
    ) {
        return create(
                document,
                actor,
                "DOCUMENT_CREATED",
                "Document checklist record created",
                "POST",
                "/api/documents",
                201
        );
    }

    public AuditEventRequest documentVerified(
            Document document,
            CurrentActor actor
    ) {
        return create(
                document,
                actor,
                "DOCUMENT_VERIFIED",
                "Document verified successfully",
                "PUT",
                "/api/documents/"
                        + document.getId()
                        + "/verification",
                200
        );
    }

    public AuditEventRequest documentRejected(
            Document document,
            CurrentActor actor
    ) {
        return create(
                document,
                actor,
                "DOCUMENT_REJECTED",
                "Document rejected successfully",
                "PUT",
                "/api/documents/"
                        + document.getId()
                        + "/verification",
                200
        );
    }

    private AuditEventRequest create(
            Document document,
            CurrentActor actor,
            String action,
            String description,
            String httpMethod,
            String endpoint,
            int statusCode
    ) {
        CurrentActor safeActor =
                actor == null
                        ? CurrentActor.anonymous()
                        : actor;

        return new AuditEventRequest(
                safeActor.userId(),
                safeActor.role(),
                action,
                ENTITY_TYPE,
                document.getId() == null
                        ? null
                        : document.getId().toString(),
                SERVICE_NAME,
                description,
                SUCCESS,
                null,
                null,
                httpMethod,
                endpoint,
                statusCode
        );
    }
}
