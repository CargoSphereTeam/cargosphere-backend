package com.cargosphere.payment.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
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
        properties =
                new AuditClientProperties();

        properties.setEnabled(true);

        properties.setBaseUrl(
                "http://localhost:8086"
        );

        properties.setApiKey(
                INTERNAL_KEY
        );

        RestClient.Builder builder =
                RestClient.builder();

        server = bindTo(builder).build();

        RestClient restClient =
                builder
                        .baseUrl(
                                properties
                                        .getBaseUrl()
                        )
                        .build();

        auditClient =
                new HttpAuditClient(
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
                .andRespond(withSuccess());

        auditClient.publish(event());

        server.verify();
    }

    @Test
    void auditFailureShouldNotEscape() {
        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:8086/api/audits/internal"
                        )
                )
                .andRespond(
                        withStatus(
                                HttpStatus
                                        .SERVICE_UNAVAILABLE
                        )
                );

        assertDoesNotThrow(() ->
                auditClient.publish(event())
        );

        server.verify();
    }

    @Test
    void missingApiKeyShouldSkipRequest() {
        properties.setApiKey(null);

        assertDoesNotThrow(() ->
                auditClient.publish(event())
        );

        server.verify();
    }

    @Test
    void disabledClientShouldSkipRequest() {
        properties.setEnabled(false);

        assertDoesNotThrow(() ->
                auditClient.publish(event())
        );

        server.verify();
    }

    private AuditEventRequest event() {
        return new AuditEventRequest(
                10L,
                "ROLE_CLIENT",
                "PAYMENT_CREATED",
                "PAYMENT",
                "55",
                "payment-service",
                "Payment created successfully with status PENDING",
                "SUCCESS",
                null,
                null,
                "POST",
                "/api/payments",
                201
        );
    }
}