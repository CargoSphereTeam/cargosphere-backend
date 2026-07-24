package com.cargosphere.payment.controller;

import com.cargosphere.payment.config.SecurityConfig;
import com.cargosphere.payment.dto.CreatePaymentRequest;
import com.cargosphere.payment.dto.PaymentResponse;
import com.cargosphere.payment.dto.RefundPaymentRequest;
import com.cargosphere.payment.dto.UpdatePaymentStatusRequest;
import com.cargosphere.payment.entity.enums.PaymentMethod;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import com.cargosphere.payment.entity.enums.PaymentType;
import com.cargosphere.payment.exception.GlobalExceptionHandler;
import com.cargosphere.payment.security.JwtUserIdExtractor;
import com.cargosphere.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        PaymentController.class,
        PaymentHealthController.class
})
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        JwtUserIdExtractor.class
})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void healthShouldRemainPublic() throws Exception {
        mockMvc.perform(
                        get("/api/payments/health")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.service")
                                .value("payment-service")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("UP")
                );

        verifyNoInteractions(paymentService);
    }

    @Test
    void anonymousUserShouldReceiveUnauthorized()
            throws Exception {

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(paymentService);
    }

    @Test
    void clientShouldCreatePayment() throws Exception {
        PaymentResponse response =
                paymentResponse(
                        PaymentStatus.PENDING
                );

        when(paymentService.createPayment(
                any(CreatePaymentRequest.class),
                eq(101L)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/payments")
                                .with(clientJwt())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "shipmentId": 1001,
                                          "amount": 2500.00,
                                          "currency": "INR",
                                          "paymentMethod": "UPI",
                                          "paymentType": "FULL",
                                          "transactionReference": "TXN-1001"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                "Location",
                                "http://localhost/api/payments/1"
                        )
                )
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.paymentStatus")
                                .value("PENDING")
                );

        verify(paymentService).createPayment(
                any(CreatePaymentRequest.class),
                eq(101L)
        );
    }

    @Test
    void clientShouldGetOwnPayments() throws Exception {
        when(paymentService.getPaymentsByUserId(101L))
                .thenReturn(List.of(
                        paymentResponse(
                                PaymentStatus.PENDING
                        )
                ));

        mockMvc.perform(
                        get("/api/payments/me")
                                .with(clientJwt())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()").value(1)
                )
                .andExpect(
                        jsonPath("$[0].userId")
                                .value(101)
                );

        verify(paymentService)
                .getPaymentsByUserId(101L);
    }

    @Test
    void clientShouldNotGetAllPayments()
            throws Exception {

        mockMvc.perform(
                        get("/api/payments")
                                .with(clientJwt())
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.status")
                                .value(403)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Access denied")
                );

        verifyNoInteractions(paymentService);
    }

    @Test
    void adminShouldGetAllPayments()
            throws Exception {

        when(paymentService.getAllPayments())
                .thenReturn(List.of(
                        paymentResponse(
                                PaymentStatus.PENDING
                        )
                ));

        mockMvc.perform(
                        get("/api/payments")
                                .with(adminJwt())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()").value(1)
                )
                .andExpect(
                        jsonPath("$[0].id").value(1)
                );

        verify(paymentService).getAllPayments();
    }

    @Test
    void adminShouldUpdatePaymentStatus()
            throws Exception {

        when(paymentService.updatePaymentStatus(
                eq(1L),
                any(UpdatePaymentStatusRequest.class)
        )).thenReturn(
                paymentResponse(PaymentStatus.PAID)
        );

        mockMvc.perform(
                        patch("/api/payments/1/status")
                                .with(adminJwt())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "paymentStatus": "PAID",
                                          "transactionReference": "TXN-1001",
                                          "remarks": "Payment received"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.paymentStatus")
                                .value("PAID")
                );

        verify(paymentService)
                .updatePaymentStatus(
                        eq(1L),
                        any(UpdatePaymentStatusRequest.class)
                );
    }

    @Test
    void adminShouldRefundPaidPayment()
            throws Exception {

        when(paymentService.refundPayment(
                eq(1L),
                any(RefundPaymentRequest.class)
        )).thenReturn(
                paymentResponse(
                        PaymentStatus.REFUNDED
                )
        );

        mockMvc.perform(
                        post("/api/payments/1/refund")
                                .with(adminJwt())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "reason": "Customer requested refund"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.paymentStatus")
                                .value("REFUNDED")
                );

        verify(paymentService).refundPayment(
                eq(1L),
                any(RefundPaymentRequest.class)
        );
    }

    @Test
    void createPaymentShouldReturnBadRequestForInvalidBody()
            throws Exception {

        mockMvc.perform(
                        post("/api/payments")
                                .with(clientJwt())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.shipmentId"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.amount"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.paymentMethod"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.paymentType"
                        ).exists()
                );

        verifyNoInteractions(paymentService);
    }

    private RequestPostProcessor clientJwt() {
        return jwt()
                .jwt(builder ->
                        builder
                                .subject(
                                        "client@example.com"
                                )
                                .claim("userId", 101L)
                                .claim(
                                        "authorities",
                                        List.of(
                                                "ROLE_CLIENT"
                                        )
                                )
                )
                .authorities(
                        new SimpleGrantedAuthority(
                                "ROLE_CLIENT"
                        )
                );
    }

    private RequestPostProcessor adminJwt() {
        return jwt()
                .jwt(builder ->
                        builder
                                .subject(
                                        "admin@cargosphere.com"
                                )
                                .claim("userId", 1L)
                                .claim(
                                        "authorities",
                                        List.of(
                                                "ROLE_ADMIN"
                                        )
                                )
                )
                .authorities(
                        new SimpleGrantedAuthority(
                                "ROLE_ADMIN"
                        )
                );
    }

    private PaymentResponse paymentResponse(
            PaymentStatus paymentStatus
    ) {
        return PaymentResponse.builder()
                .id(1L)
                .shipmentId(1001L)
                .userId(
                        paymentStatus
                                == PaymentStatus.PENDING
                                ? 101L
                                : 1L
                )
                .amount(
                        new BigDecimal("2500.00")
                )
                .currency("INR")
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus(paymentStatus)
                .paymentType(PaymentType.FULL)
                .transactionReference("TXN-1001")
                .build();
    }
}