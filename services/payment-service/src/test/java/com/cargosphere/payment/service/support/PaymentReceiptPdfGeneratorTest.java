package com.cargosphere.payment.service.support;

import com.cargosphere.payment.dto.ShipmentPaymentSummaryResponse;
import com.cargosphere.payment.entity.enums.PaymentMethod;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentReceiptPdfGeneratorTest {

    @Test
    void generatesReadablePaymentReceipt() throws Exception {
        ShipmentPaymentSummaryResponse summary = ShipmentPaymentSummaryResponse.builder()
                .shipmentId(9L)
                .currency("INR")
                .finalAmount(new BigDecimal("12500.00"))
                .paidAmount(new BigDecimal("12500.00"))
                .balanceAmount(new BigDecimal("0.00"))
                .paymentMethod(PaymentMethod.UPI)
                .confirmedAt(LocalDateTime.of(2026, 8, 6, 20, 30))
                .paymentConfirmed(true)
                .build();

        byte[] pdf = new PaymentReceiptPdfGenerator().generate(9L, summary);
        String output = System.getProperty("payment.receipt.output");
        if (output != null) {
            Path path = Path.of(output);
            Files.createDirectories(path.getParent());
            Files.write(path, pdf);
        }

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertThat(document.getNumberOfPages()).isEqualTo(1);
            assertThat(text)
                    .contains("Payment receipt")
                    .contains("INR 12500.00")
                    .contains("SHIPMENT ID")
                    .contains("CONFIRMED");
        }
    }
}
