package com.cargosphere.payment.service.impl;

import com.cargosphere.payment.audit.PaymentAuditPublisher;
import com.cargosphere.payment.dto.RazorpayOrderResponse;
import com.cargosphere.payment.dto.RazorpayVerificationRequest;
import com.cargosphere.payment.dto.ShipmentPaymentSummaryResponse;
import com.cargosphere.payment.entity.Payment;
import com.cargosphere.payment.entity.ShipmentPaymentSummary;
import com.cargosphere.payment.entity.enums.ConfirmationStatus;
import com.cargosphere.payment.entity.enums.PaymentMethod;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import com.cargosphere.payment.entity.enums.PaymentType;
import com.cargosphere.payment.exception.InvalidPaymentAmountException;
import com.cargosphere.payment.exception.InvalidPaymentStateException;
import com.cargosphere.payment.exception.PaymentNotFoundException;
import com.cargosphere.payment.mapper.ShipmentPaymentSummaryMapper;
import com.cargosphere.payment.repository.PaymentRepository;
import com.cargosphere.payment.repository.ShipmentPaymentSummaryRepository;
import com.cargosphere.payment.service.RazorpayCheckoutService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;

@Service
@Transactional
public class RazorpayCheckoutServiceImpl implements RazorpayCheckoutService {

    private final ShipmentPaymentSummaryRepository summaryRepository;
    private final PaymentRepository paymentRepository;
    private final ShipmentPaymentSummaryMapper summaryMapper;
    private final PaymentAuditPublisher auditPublisher;
    private final RestClient razorpayClient;
    private final String keyId;
    private final String keySecret;

    public RazorpayCheckoutServiceImpl(
            ShipmentPaymentSummaryRepository summaryRepository,
            PaymentRepository paymentRepository,
            ShipmentPaymentSummaryMapper summaryMapper,
            PaymentAuditPublisher auditPublisher,
            RestClient.Builder restClientBuilder,
            @Value("${app.razorpay.key-id:}") String keyId,
            @Value("${app.razorpay.key-secret:}") String keySecret,
            @Value("${app.razorpay.api-url:https://api.razorpay.com/v1}") String apiUrl
    ) {
        this.summaryRepository = summaryRepository;
        this.paymentRepository = paymentRepository;
        this.summaryMapper = summaryMapper;
        this.auditPublisher = auditPublisher;
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.razorpayClient = restClientBuilder
                .baseUrl(apiUrl)
                .defaultHeaders(headers -> headers.setBasicAuth(keyId, keySecret))
                .build();
    }

    @Override
    public RazorpayOrderResponse createOrder(Long shipmentId, Long userId) {
        requireConfigured();
        ShipmentPaymentSummary summary = findSummary(shipmentId);

        if (summary.getConfirmationStatus() == ConfirmationStatus.CONFIRMED) {
            throw new InvalidPaymentStateException("This shipment has already been paid.");
        }
        if (summary.getConfirmationStatus() != ConfirmationStatus.APPROVED) {
            throw new InvalidPaymentStateException(
                    "Payment details are pending administrator approval."
            );
        }
        if (summary.getFinalAmount() == null
                || summary.getFinalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentAmountException("A positive final amount is required before payment.");
        }

        long amountInPaise = summary.getFinalAmount()
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();
        String receipt = "shipment-" + shipmentId + "-" + System.currentTimeMillis();

        RazorpayOrder order = razorpayClient.post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "amount", amountInPaise,
                        "currency", summary.getCurrency(),
                        "receipt", receipt,
                        "notes", Map.of("shipmentId", shipmentId.toString())
                ))
                .retrieve()
                .body(RazorpayOrder.class);

        if (order == null || order.id() == null) {
            throw new InvalidPaymentStateException("Razorpay did not return a valid order.");
        }

        Payment payment = Payment.builder()
                .shipmentId(shipmentId)
                .userId(userId)
                .amount(summary.getFinalAmount())
                .currency(summary.getCurrency())
                .paymentMethod(PaymentMethod.OTHER)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentType(PaymentType.FULL)
                .transactionReference(order.id())
                .remarks("Razorpay checkout created")
                .build();
        Payment saved = paymentRepository.save(payment);
        auditPublisher.publishPaymentCreated(saved);

        return new RazorpayOrderResponse(
                keyId,
                order.id(),
                amountInPaise,
                summary.getCurrency(),
                shipmentId,
                "CargoSphere shipment " + shipmentId
        );
    }

    @Override
    public ShipmentPaymentSummaryResponse verifyPayment(
            Long shipmentId,
            RazorpayVerificationRequest request,
            Long userId
    ) {
        requireConfigured();
        verifySignature(request);

        Payment payment = paymentRepository
                .findByTransactionReference(request.razorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException("Razorpay order was not found."));

        if (!payment.getShipmentId().equals(shipmentId)
                || !payment.getUserId().equals(userId)) {
            throw new InvalidPaymentStateException("Payment does not belong to this shipment and user.");
        }

        ShipmentPaymentSummary summary = findSummary(shipmentId);
        if (payment.getAmount().compareTo(summary.getFinalAmount()) != 0) {
            throw new InvalidPaymentAmountException("Paid amount does not match the shipment final amount.");
        }

        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            PaymentStatus previousStatus = payment.getPaymentStatus();
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidDate(LocalDate.now());
            payment.setRemarks("Razorpay payment: " + request.razorpayPaymentId());
            paymentRepository.save(payment);
            auditPublisher.publishPaymentStatusUpdated(payment, previousStatus);
        }

        summary.setPaidAmount(summary.getFinalAmount());
        summary.setBalanceAmount(BigDecimal.ZERO.setScale(2));
        summary.setPaymentMethod(PaymentMethod.OTHER);
        summary.setConfirmationStatus(ConfirmationStatus.CONFIRMED);
        summary.setConfirmedBy(userId);
        summary.setConfirmedAt(LocalDateTime.now());
        summary.setRemarks("Paid online through Razorpay");
        ShipmentPaymentSummary savedSummary = summaryRepository.save(summary);
        auditPublisher.publishShipmentPaymentConfirmed(savedSummary);

        return summaryMapper.toResponse(savedSummary);
    }

    private ShipmentPaymentSummary findSummary(Long shipmentId) {
        return summaryRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment is not ready yet. The administrator must save the shipment amount first."
                ));
    }

    private void verifySignature(RazorpayVerificationRequest request) {
        try {
            String payload = request.razorpayOrderId() + "|" + request.razorpayPaymentId();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            byte[] actual = HexFormat.of().parseHex(request.razorpaySignature());

            if (!MessageDigest.isEqual(expected, actual)) {
                throw new InvalidPaymentStateException("Razorpay payment signature is invalid.");
            }
        } catch (InvalidPaymentStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidPaymentStateException("Unable to verify the Razorpay payment signature.");
        }
    }

    private void requireConfigured() {
        if (keyId.isBlank() || keySecret.isBlank()) {
            throw new InvalidPaymentStateException("Razorpay is not configured.");
        }
    }

    private record RazorpayOrder(String id) {
    }
}
