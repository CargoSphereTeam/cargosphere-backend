package com.cargosphere.documentservice.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpAuditClientTest {

    private static final String INTERNAL_KEY =
            "test-internal-audit-key-0123456789abcdef";

    private AuditClientProperties properties;

    private MockRestServiceServer server;

    private HttpAuditClient auditClient;

    @BeforeEach
    void setUp() {
        properties = new AuditClientProperties();

        properties.setEnabled(true);
        properties.setBaseUrl(
                "http://localhost:8086/"
        );
        properties.setApiKey(INTERNAL_KEY);

        RestClient.Builder builder =
                RestClient.builder();

        server = MockRestServiceServer
                .bindTo(builder)
                .build();

        RestClient restClient = builder
                .baseUrl(
                        properties.getBaseUrl()
                )
                .build();

        auditClient = new HttpAuditClient(
                restClient,
                properties
        );
    }

    @Test
    void publishShouldSendInternalRequest() {
        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:8086/api/audits/internal"
                        )
                )
                .andExpect(
                        method(HttpMethod.POST)
                )
                .andExpect(
                        header(
                                "X-Internal-API-Key",
                                INTERNAL_KEY
                        )
                )
                .andRespond(
                        withSuccess()
                );

        auditClient.publish(event());

        server.verify();
    }

    @Test
    void auditServiceFailureShouldNotEscape() {
        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:8086/api/audits/internal"
                        )
                )
                .andExpect(
                        method(HttpMethod.POST)
                )
                .andRespond(
                        withStatus(
                                HttpStatus.SERVICE_UNAVAILABLE
                        )
                );

        assertDoesNotThrow(
                () -> auditClient.publish(event())
        );

        server.verify();
    }

    @Test
    void disabledClientShouldNotSendRequest() {
        properties.setEnabled(false);

        assertDoesNotThrow(
                () -> auditClient.publish(event())
        );

        server.verify();
    }

    @Test
    void missingApiKeyShouldNotSendRequest() {
        properties.setApiKey(" ");

        assertDoesNotThrow(
                () -> auditClient.publish(event())
        );

        server.verify();
    }

    @Test
    void nullEventShouldNotSendRequest() {
        assertDoesNotThrow(
                () -> auditClient.publish(null)
        );

        server.verify();
    }

    private AuditEventRequest event() {
        return new AuditEventRequest(
                10L,
                "ROLE_CLIENT",
                "DOCUMENT_CREATED",
                "DOCUMENT",
                "55",
                "document-service",
                "Document checklist record created",
                "SUCCESS",
                null,
                null,
                "POST",
                "/api/documents",
                201
        );
    }
}