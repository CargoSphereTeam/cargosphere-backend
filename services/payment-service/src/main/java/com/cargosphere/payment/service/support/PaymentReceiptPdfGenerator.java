package com.cargosphere.payment.service.support;

import com.cargosphere.payment.dto.ShipmentPaymentSummaryResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class PaymentReceiptPdfGenerator {

    private static final PDType1Font REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public byte[] generate(Long shipmentId, ShipmentPaymentSummaryResponse summary) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDDocumentInformation information = new PDDocumentInformation();
            information.setTitle("CargoSphere payment receipt - shipment " + shipmentId);
            information.setAuthor("CargoSphere");
            document.setDocumentInformation(information);

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.setNonStrokingColor(31 / 255F, 107 / 255F, 76 / 255F);
                write(content, BOLD, 13, 52, 790, "CARGOSPHERE");
                content.setNonStrokingColor(28 / 255F, 37 / 255F, 33 / 255F);
                write(content, BOLD, 25, 52, 738, "Payment receipt");
                write(content, REGULAR, 10, 52, 714, "Payment successfully received and verified");

                content.setNonStrokingColor(242 / 255F, 247 / 255F, 243 / 255F);
                content.addRect(52, 585, 491, 95);
                content.fill();
                content.setNonStrokingColor(28 / 255F, 37 / 255F, 33 / 255F);
                write(content, BOLD, 11, 72, 651, "AMOUNT PAID");
                write(content, BOLD, 24, 72, 617,
                        value(summary.getCurrency()) + " " + value(summary.getPaidAmount()));

                row(content, 548, "Shipment ID", shipmentId);
                row(content, 516, "Payment status", "CONFIRMED");
                row(content, 484, "Final amount", summary.getFinalAmount());
                row(content, 452, "Balance amount", summary.getBalanceAmount());
                row(content, 420, "Payment method", summary.getPaymentMethod());
                row(content, 388, "Confirmed at", summary.getConfirmedAt());

                content.setStrokingColor(215 / 255F, 224 / 255F, 218 / 255F);
                content.moveTo(52, 75);
                content.lineTo(543, 75);
                content.stroke();
                content.setNonStrokingColor(93 / 255F, 107 / 255F, 99 / 255F);
                write(content, REGULAR, 8, 52, 55,
                        "This receipt was generated from a verified CargoSphere payment record.");
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate payment receipt PDF", exception);
        }
    }

    private void row(PDPageContentStream content, float y, String label, Object value)
            throws IOException {
        content.setNonStrokingColor(93 / 255F, 107 / 255F, 99 / 255F);
        write(content, BOLD, 9, 52, y, label.toUpperCase());
        content.setNonStrokingColor(28 / 255F, 37 / 255F, 33 / 255F);
        write(content, REGULAR, 10, 235, y, value(value));
    }

    private void write(PDPageContentStream content, PDType1Font font,
                       float size, float x, float y, String text) throws IOException {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(value(text).replaceAll("[^\\x20-\\x7E]", "?"));
        content.endText();
    }

    private String value(Object value) {
        return value == null ? "N/A" : value.toString();
    }
}
