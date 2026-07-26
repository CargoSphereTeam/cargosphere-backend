package com.cargosphere.documentservice.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class HttpAuditClient
        implements AuditClient {

    private static final String INTERNAL_ENDPOINT =
            "/api/audits/internal";

    private static final String INTERNAL_KEY_HEADER =
            "X-Internal-API-Key";

    private final RestClient restClient;

    private final AuditClientProperties properties;

    public HttpAuditClient(
            @Qualifier("auditRestClient")
            RestClient restClient,
            AuditClientProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public void publish(
            AuditEventRequest request
    ) {
        if (!properties.isEnabled()) {
            return;
        }

        if (request == null) {
            log.warn(
                    "Audit publishing skipped because event request is null"
            );
            return;
        }

        if (
                properties.getApiKey() == null
                        || properties.getApiKey().isBlank()
        ) {
            log.warn(
                    "Audit publishing skipped because internal API key is not configured"
            );
            return;
        }

        try {
            restClient.post()
                    .uri(INTERNAL_ENDPOINT)
                    .header(
                            INTERNAL_KEY_HEADER,
                            properties.getApiKey()
                    )
                    .contentType(
                            MediaType.APPLICATION_JSON
                    )
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            log.warn(
                    "Audit delivery failed: action={}, entityType={}, entityId={}, reason={}",
                    request.action(),
                    request.entityType(),
                    request.entityId(),
                    exception.getClass().getSimpleName()
            );
        }
    }
}