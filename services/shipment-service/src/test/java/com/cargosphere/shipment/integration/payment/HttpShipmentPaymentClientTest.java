package com.cargosphere.shipment.integration.payment;

import com.cargosphere.shipment.security.CurrentJwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpShipmentPaymentClientTest {

    @Test
    void shouldReturnShipmentPayments() {
        RestClient.Builder builder =
                RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer
                        .bindTo(builder)
                        .build();

        RestClient restClient = builder
                .baseUrl("http://localhost:8085")
                .build();

        CurrentJwtTokenProvider tokenProvider =
                mock(CurrentJwtTokenProvider.class);

        when(tokenProvider.getTokenValue())
                .thenReturn("admin-token");

        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:8085/api/payments/shipment/101"
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
                                    "userId": 5,
                                    "amount": 12500.00,
                                    "currency": "INR",
                                    "paymentMethod": "UPI",
                                    "paymentStatus": "PAID",
                                    "paymentType": "FULL",
                                    "transactionReference": "TXN-2026-000123",
                                    "dueDate": "2026-08-15",
                                    "paidDate": "2026-08-02",
                                    "remarks": "Payment confirmed",
                                    "createdAt": "2026-08-02T09:00:00",
                                    "updatedAt": "2026-08-02T10:00:00"
                                  }
                                ]
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );

        ShipmentPaymentClient client =
                new HttpShipmentPaymentClient(
                        restClient,
                        tokenProvider
                );

        List<ShipmentPaymentResponse> response =
                client.getPaymentsByShipmentId(101L);

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
                new BigDecimal("12500.00"),
                response.getFirst().amount()
        );
        assertEquals(
                "PAID",
                response.getFirst().paymentStatus()
        );
        assertEquals(
                LocalDate.of(2026, 8, 2),
                response.getFirst().paidDate()
        );

        server.verify();
    }
}
