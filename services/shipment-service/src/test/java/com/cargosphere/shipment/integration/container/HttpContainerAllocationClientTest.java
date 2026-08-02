package com.cargosphere.shipment.integration.container;

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

class HttpContainerAllocationClientTest {

    @Test
    void shouldReturnShipmentContainerAllocations() {
        RestClient.Builder builder =
                RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer
                        .bindTo(builder)
                        .build();

        RestClient restClient = builder
                .baseUrl("http://localhost:8083")
                .build();

        CurrentJwtTokenProvider tokenProvider =
                mock(CurrentJwtTokenProvider.class);

        when(tokenProvider.getTokenValue())
                .thenReturn("admin-token");

        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:8083/api/container-allocations/shipment/101"
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
                                    "allocationId": 1,
                                    "shipmentId": 101,
                                    "containerTypeId": 2,
                                    "containerTypeCode": "DRY_20",
                                    "containerTypeName": "20 Foot Dry Container",
                                    "quantity": 2,
                                    "allocationStatus": "ALLOCATED",
                                    "notes": "Reserved",
                                    "allocatedAt": "2026-08-02T10:00:00",
                                    "updatedAt": "2026-08-02T10:00:00"
                                  }
                                ]
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );

        ContainerAllocationClient client =
                new HttpContainerAllocationClient(
                        restClient,
                        tokenProvider
                );

        List<ContainerAllocationResponse> response =
                client.getAllocationsByShipmentId(101L);

        assertEquals(1, response.size());
        assertEquals(
                1L,
                response.getFirst().allocationId()
        );
        assertEquals(
                101L,
                response.getFirst().shipmentId()
        );
        assertEquals(
                2,
                response.getFirst().quantity()
        );

        server.verify();
    }
}
