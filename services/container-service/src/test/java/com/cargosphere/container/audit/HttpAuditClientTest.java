package com.cargosphere.container.audit;

import com.cargosphere.container.config.AuditClientProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpAuditClientTest {

    private AuditClientProperties properties;

    private MockRestServiceServer server;

    private HttpAuditClient auditClient;

    private AuditEventRequest request;

    @BeforeEach
    void setUp() {
        properties = new AuditClientProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("http://localhost:8086");
        properties.setApiKey("test-internal-key");

        RestClient.Builder builder = RestClient.builder();

        server = MockRestServiceServer
                .bindTo(builder)
                .build();

        RestClient restClient = builder
                .baseUrl(properties.getBaseUrl())
                .build();

        auditClient = new HttpAuditClient(
                restClient,
                properties
        );

        request = new AuditEventRequest(
                1L,
                "ROLE_ADMIN",
                "CONTAINER_ALLOCATED",
                "CONTAINER",
                "10",
                "container-service",
                "Container allocation created for shipment 100",
                "SUCCESS",
                null,
                null,
                "POST",
                "/api/container-allocations",
                201
        );
    }

    @Test
    void send_shouldPostExpectedRequest() {
        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:8086/api/audits/internal"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(
                        "X-Internal-API-Key",
                        "test-internal-key"
                ))
                .andExpect(content().contentType(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(content().json(
                        """
                        {
                          "actorUserId": 1,
                          "actorRole": "ROLE_ADMIN",
                          "action": "CONTAINER_ALLOCATED",
                          "entityType": "CONTAINER",
                          "entityId": "10",
                          "serviceName": "container-service",
                          "description": "Container allocation created for shipment 100",
                          "outcome": "SUCCESS",
                          "requestId": null,
                          "ipAddress": null,
                          "httpMethod": "POST",
                          "endpoint": "/api/container-allocations",
                          "statusCode": 201
                        }
                        """
                ))
                .andRespond(withSuccess());

        auditClient.send(request);

        server.verify();
    }

    @Test
    void send_whenDisabled_shouldSkipDelivery() {
        properties.setEnabled(false);

        assertDoesNotThrow(
                () -> auditClient.send(request)
        );

        server.verify();
    }

    @Test
    void send_whenApiKeyMissing_shouldSkipDelivery() {
        properties.setApiKey("");

        assertDoesNotThrow(
                () -> auditClient.send(request)
        );

        server.verify();
    }

    @Test
    void send_whenAuditServiceReturnsError_shouldNotThrow() {
        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:8086/api/audits/internal"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertDoesNotThrow(
                () -> auditClient.send(request)
        );

        server.verify();
    }
}