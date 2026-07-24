package com.cargosphere.payment.controller;

import com.cargosphere.payment.dto.CreatePaymentRequest;
import com.cargosphere.payment.dto.PaymentResponse;
import com.cargosphere.payment.dto.RefundPaymentRequest;
import com.cargosphere.payment.dto.UpdatePaymentStatusRequest;
import com.cargosphere.payment.security.JwtUserIdExtractor;
import com.cargosphere.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtUserIdExtractor jwtUserIdExtractor;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid
            @RequestBody
            CreatePaymentRequest request,

            @AuthenticationPrincipal
            Jwt jwt
    ) {
        Long userId =
                jwtUserIdExtractor.extractUserId(jwt);

        PaymentResponse response =
                paymentService.createPayment(
                        request,
                        userId
                );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>>
    getAllPayments() {

        return ResponseEntity.ok(
                paymentService.getAllPayments()
        );
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<PaymentResponse>>
    getMyPayments(
            @AuthenticationPrincipal
            Jwt jwt
    ) {
        Long userId =
                jwtUserIdExtractor.extractUserId(jwt);

        return ResponseEntity.ok(
                paymentService.getPaymentsByUserId(
                        userId
                )
        );
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse>
    getPaymentById(
            @PathVariable
            @Positive(message = "Payment ID must be positive")
            Long paymentId
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentById(
                        paymentId
                )
        );
    }

    @GetMapping("/shipment/{shipmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>>
    getPaymentsByShipmentId(
            @PathVariable
            @Positive(message = "Shipment ID must be positive")
            Long shipmentId
    ) {
        return ResponseEntity.ok(
                paymentService
                        .getPaymentsByShipmentId(
                                shipmentId
                        )
        );
    }

    @PatchMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse>
    updatePaymentStatus(
            @PathVariable
            @Positive(message = "Payment ID must be positive")
            Long paymentId,

            @Valid
            @RequestBody
            UpdatePaymentStatusRequest request
    ) {
        return ResponseEntity.ok(
                paymentService.updatePaymentStatus(
                        paymentId,
                        request
                )
        );
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse>
    refundPayment(
            @PathVariable
            @Positive(message = "Payment ID must be positive")
            Long paymentId,

            @Valid
            @RequestBody
            RefundPaymentRequest request
    ) {
        return ResponseEntity.ok(
                paymentService.refundPayment(
                        paymentId,
                        request
                )
        );
    }
}