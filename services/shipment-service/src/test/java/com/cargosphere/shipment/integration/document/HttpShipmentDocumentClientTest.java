package com.cargosphere.shipment.integration.document;

import com.cargosphere.shipment.security.CurrentJwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpShipmentDocumentClientTest {

    @Test
    void shouldReturnShipmentDocuments() {
        RestClient.Builder builder =
                RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer
                        .bindTo(builder)
                        .build();

        RestClient restClient = builder
                .baseUrl("http://localhost:8084")
                .build();

        CurrentJwtTokenProvider tokenProvider =
                mock(CurrentJwtTokenProvider.class);

        when(tokenProvider.getTokenValue())
                .thenReturn("admin-token");

        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:8084/api/documents/shipment/101"
                        )
                )
                .andExpect(
                        header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer admin-token"
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                [
                                  {
                                    "id": 1,
                                    "shipmentId": 101,
                                    "documentType": "COMMERCIAL_INVOICE",
                                    "required": true,
                                    "verificationStatus": "VERIFIED",
                                    "verifiedBy": 5,
                                    "verifiedAt": "2026-08-02T10:00:00",
                                    "remarks": "Verified",
                                    "createdAt": "2026-08-02T09:00:00",
                                    "updatedAt": "2026-08-02T10:00:00"
                                  }
                                ]
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );

        ShipmentDocumentClient client =
                new HttpShipmentDocumentClient(
                        restClient,
                        tokenProvider
                );

        List<ShipmentDocumentResponse> response =
                client.getDocumentsByShipmentId(101L);

        assertEquals(1, response.size());
        assertEquals(
                1L,
                response.getFirst().id()
        );
        assertEquals(
                101L,
                response.getFirst().shipmentId()
        );
        assertEquals(
                "VERIFIED",
                response.getFirst().verificationStatus()
        );
        assertEquals(
                true,
                response.getFirst().required()
        );

        server.verify();
    }
}
