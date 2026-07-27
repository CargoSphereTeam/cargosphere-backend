package com.cargosphere.container.audit;

import com.cargosphere.container.config.AuditClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class HttpAuditClient implements AuditClient {

    private static final String INTERNAL_AUDIT_ENDPOINT =
            "/api/audits/internal";

    private static final String INTERNAL_API_KEY_HEADER =
            "X-Internal-API-Key";

    private final RestClient auditRestClient;
    private final AuditClientProperties properties;

    public HttpAuditClient(
            @Qualifier("auditRestClient")
            RestClient auditRestClient,
            AuditClientProperties properties
    ) {
        this.auditRestClient = auditRestClient;
        this.properties = properties;
    }

    @Override
    public void send(AuditEventRequest request) {
        if (!properties.isEnabled()) {
            log.debug(
                    "Audit delivery is disabled; action={} entityType={} entityId={}",
                    request.action(),
                    request.entityType(),
                    request.entityId()
            );
            return;
        }

        if (!properties.hasApiKey()) {
            log.warn(
                    "Audit delivery skipped because internal API key is not configured; "
                            + "action={} entityType={} entityId={}",
                    request.action(),
                    request.entityType(),
                    request.entityId()
            );
            return;
        }

        try {
            auditRestClient
                    .post()
                    .uri(INTERNAL_AUDIT_ENDPOINT)
                    .header(
                            INTERNAL_API_KEY_HEADER,
                            properties.getApiKey()
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.debug(
                    "Audit event delivered; action={} entityType={} entityId={}",
                    request.action(),
                    request.entityType(),
                    request.entityId()
            );
        } catch (RestClientException exception) {
            log.warn(
                    "Audit delivery failed safely; action={} entityType={} "
                            + "entityId={} reason={}",
                    request.action(),
                    request.entityType(),
                    request.entityId(),
                    safeMessage(exception)
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "Unexpected audit delivery failure handled safely; "
                            + "action={} entityType={} entityId={} reason={}",
                    request.action(),
                    request.entityType(),
                    request.entityId(),
                    safeMessage(exception)
            );
        }
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return message;
    }
}