package com.cargosphere.payment.service.impl;

import com.cargosphere.payment.dto.ShipmentPaymentSummaryResponse;
import com.cargosphere.payment.service.PaymentNotificationService;
import com.cargosphere.payment.service.support.PaymentReceiptPdfGenerator;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class SmtpPaymentNotificationService implements PaymentNotificationService {

    private final JavaMailSender mailSender;
    private final PaymentReceiptPdfGenerator receiptGenerator;
    private final RestClient shipmentClient;
    private final RestClient authClient;
    private final boolean enabled;
    private final String from;
    private final String frontendUrl;

    public SmtpPaymentNotificationService(
            JavaMailSender mailSender,
            PaymentReceiptPdfGenerator receiptGenerator,
            RestClient.Builder restClientBuilder,
            @Value("${app.payment-mail.enabled:false}") boolean enabled,
            @Value("${app.payment-mail.from:}") String from,
            @Value("${app.payment-mail.frontend-url:http://localhost:5173}") String frontendUrl,
            @Value("${app.payment-mail.shipment-service-url:http://localhost:8082}") String shipmentUrl,
            @Value("${app.payment-mail.auth-service-url:http://localhost:8081}") String authUrl
    ) {
        this.mailSender = mailSender;
        this.receiptGenerator = receiptGenerator;
        this.enabled = enabled;
        this.from = from;
        this.frontendUrl = frontendUrl;
        this.shipmentClient = restClientBuilder.baseUrl(shipmentUrl).build();
        this.authClient = restClientBuilder.baseUrl(authUrl).build();
    }

    @Override
    public void sendPaymentRequest(
            Long shipmentId,
            ShipmentPaymentSummaryResponse summary,
            String bearerToken
    ) {
        if (!canSend()) return;
        try {
            ShipmentContact shipment = getShipment(shipmentId, bearerToken);
            UserContact client = authClient.get()
                    .uri("/api/auth/users/{id}", shipment.clientUserId())
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .retrieve()
                    .body(UserContact.class);
            if (client == null) return;

            String paymentUrl = frontendUrl + "/client/shipments/" + shipmentId;
            String subject = "Payment requested for " + shipment.shipmentNumber();
            String html = """
                    <h2>CargoSphere payment request</h2>
                    <p>Hello %s,</p>
                    <p>The administrator approved the payment details for shipment <strong>%s</strong>.</p>
                    <p><strong>Amount due: %s %s</strong></p>
                    <p><a href="%s">Open shipment and pay securely</a></p>
                    <p>The shipment remains pending until your payment is verified.</p>
                    """.formatted(
                    escape(client.fullName()),
                    escape(shipment.shipmentNumber()),
                    escape(summary.getCurrency()),
                    escape(summary.getFinalAmount()),
                    escape(paymentUrl)
            );
            send(client.email(), subject, html, null, null);
        } catch (Exception exception) {
            log.warn("Unable to send payment request email for shipment {}: {}",
                    shipmentId, exception.getMessage());
        }
    }

    @Override
    public void sendPaymentReceipt(
            Long shipmentId,
            ShipmentPaymentSummaryResponse summary,
            String bearerToken
    ) {
        if (!canSend()) return;
        try {
            ShipmentContact shipment = getShipment(shipmentId, bearerToken);
            UserContact client = authClient.get()
                    .uri("/api/auth/profile")
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .retrieve()
                    .body(UserContact.class);
            if (client == null) return;

            String subject = "Payment received for " + shipment.shipmentNumber();
            String html = """
                    <h2>Payment successful</h2>
                    <p>Hello %s,</p>
                    <p>We received and verified your payment for shipment <strong>%s</strong>.</p>
                    <p><strong>Amount paid: %s %s</strong></p>
                    <p>Your payment receipt is attached to this email.</p>
                    """.formatted(
                    escape(client.fullName()),
                    escape(shipment.shipmentNumber()),
                    escape(summary.getCurrency()),
                    escape(summary.getPaidAmount())
            );
            byte[] receipt = receiptGenerator.generate(shipmentId, summary);
            send(
                    client.email(),
                    subject,
                    html,
                    "CargoSphere-payment-receipt-" + shipmentId + ".pdf",
                    receipt
            );
        } catch (Exception exception) {
            log.warn("Unable to send payment receipt email for shipment {}: {}",
                    shipmentId, exception.getMessage());
        }
    }

    private ShipmentContact getShipment(Long shipmentId, String token) {
        return shipmentClient.get()
                .uri("/api/shipments/{id}", shipmentId)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .body(ShipmentContact.class);
    }

    private void send(String to, String subject, String html,
                      String attachmentName, byte[] attachment) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                attachment != null,
                StandardCharsets.UTF_8.name()
        );
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        if (attachment != null) {
            helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
        }
        mailSender.send(message);
    }

    private boolean canSend() {
        if (!enabled || from == null || from.isBlank()) {
            log.debug("SMTP payment notification skipped because mail is disabled or sender is missing");
            return false;
        }
        return true;
    }

    private String escape(Object value) {
        if (value == null) return "";
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private record ShipmentContact(Long id, String shipmentNumber, Long clientUserId) { }

    private record UserContact(Long id, String fullName, String email) { }
}
