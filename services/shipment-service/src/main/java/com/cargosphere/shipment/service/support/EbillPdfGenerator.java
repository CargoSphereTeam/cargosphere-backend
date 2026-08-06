package com.cargosphere.shipment.service.support;

import com.cargosphere.shipment.dto.ebill.snapshot.*;
import com.cargosphere.shipment.exception.EbillPdfGenerationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

@Component
public class EbillPdfGenerator {

    private static final PDFont REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public byte[] generate(EbillSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("eBill snapshot must not be null");
        }

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            configureDocumentInformation(document, snapshot);
            PdfLayout layout = new PdfLayout(document, snapshot.ebillNumber());

            layout.start();
            layout.title("CARGOSPHERE", "ELECTRONIC BILL OF LADING");
            layout.hero(snapshot);
            writeShipment(layout, snapshot.shipment());
            writeClient(layout, snapshot.client());
            writeReadiness(layout, snapshot.readiness());
            writeOriginalCargo(layout, snapshot.originalCargo());
            writeConfirmedCargo(layout, snapshot.confirmedCargo());
            writeContainers(layout, snapshot.containerAllocations());
            writeDocuments(layout, snapshot.documents());
            writePayments(layout, snapshot.payments());
            writeEvents(layout, snapshot.shipmentEvents());
            layout.declaration(snapshot.schemaVersion());
            layout.finish();

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new EbillPdfGenerationException("Failed to generate eBill PDF", exception);
        }
    }

    private void writeShipment(PdfLayout out, EbillShipmentSnapshot value) throws IOException {
        out.section("Shipment information");
        if (value == null) {
            out.empty("Shipment information is not available.");
            return;
        }
        out.grid(List.of(
                pair("Shipment number", value.shipmentNumber()),
                pair("Shipment ID", value.shipmentId()),
                pair("Origin", value.originLocation()),
                pair("Destination", value.destinationLocation()),
                pair("Shipment type", value.shipmentType()),
                pair("Status", value.status()),
                pair("Processing stage", value.processingStage()),
                pair("Expected pickup", value.expectedPickupDate()),
                pair("Expected delivery", value.expectedDeliveryDate()),
                pair("Processing started", value.processingStartedAt())
        ));
    }

    private void writeClient(PdfLayout out, EbillClientSnapshot value) throws IOException {
        out.section("Client information");
        if (value == null) {
            out.empty("Client information is not available.");
            return;
        }
        out.grid(List.of(
                pair("Client name", value.fullName()), pair("Client ID", value.userId()),
                pair("Email", value.email()), pair("Phone", value.phoneNumber()),
                pair("Account role", value.role()), pair("Account status", value.status())
        ));
    }

    private void writeReadiness(PdfLayout out, EbillReadinessSnapshot value) throws IOException {
        out.section("Processing readiness");
        if (value == null) {
            out.empty("Readiness information is not available.");
            return;
        }
        out.grid(List.of(
                pair("Current stage", value.processingStage()),
                pair("eBill ready", yesNo(value.ebillReady())),
                pair("Container", ready(value.containerReady())),
                pair("Cargo", ready(value.cargoReady())),
                pair("Documents", ready(value.documentsReady())),
                pair("Payment", ready(value.paymentReady()))
        ));
        if (!value.blockingReasons().isEmpty()) {
            out.note("Blocking reasons", String.join("; ", value.blockingReasons()));
        }
    }

    private void writeOriginalCargo(PdfLayout out, List<EbillOriginalCargoSnapshot> values) throws IOException {
        out.section("Declared cargo");
        if (values.isEmpty()) {
            out.empty("No declared cargo records.");
            return;
        }
        int index = 1;
        for (EbillOriginalCargoSnapshot value : values) {
            out.card("Cargo " + index++ + " - " + printable(value.cargoName()), List.of(
                    pair("Type", value.cargoType()), pair("Quantity", value.quantity()),
                    pair("Weight", measure(value.weightKg(), "kg")),
                    pair("Volume", measure(value.volumeCbm(), "CBM")),
                    pair("Fragile", yesNo(value.fragile())), pair("Hazardous", yesNo(value.hazardous())),
                    pair("Description", value.cargoDescription())
            ));
        }
    }

    private void writeConfirmedCargo(PdfLayout out, List<EbillConfirmedCargoSnapshot> values) throws IOException {
        out.section("Verified cargo");
        if (values.isEmpty()) {
            out.empty("No verified cargo records.");
            return;
        }
        int index = 1;
        for (EbillConfirmedCargoSnapshot value : values) {
            out.card("Verified cargo " + index++ + " - " + printable(value.confirmedCargoName()), List.of(
                    pair("Verification", value.verificationStatus()), pair("Type", value.confirmedCargoType()),
                    pair("Quantity", value.confirmedQuantity()),
                    pair("Weight", measure(value.confirmedWeightKg(), "kg")),
                    pair("Volume", measure(value.confirmedVolumeCbm(), "CBM")),
                    pair("Fragile", yesNo(value.confirmedFragile())),
                    pair("Hazardous", yesNo(value.confirmedHazardous())),
                    pair("Verified at", value.verifiedAt()), pair("Remarks", value.verificationRemarks())
            ));
        }
    }

    private void writeContainers(PdfLayout out, List<EbillContainerAllocationSnapshot> values) throws IOException {
        out.section("Container allocation");
        if (values.isEmpty()) {
            out.empty("No container allocations.");
            return;
        }
        for (EbillContainerAllocationSnapshot value : values) {
            out.card(printable(value.containerTypeName()), List.of(
                    pair("Container code", value.containerTypeCode()), pair("Quantity", value.quantity()),
                    pair("Status", value.allocationStatus()), pair("Allocated at", value.allocatedAt()),
                    pair("Notes", value.notes())
            ));
        }
    }

    private void writeDocuments(PdfLayout out, List<EbillDocumentSnapshot> values) throws IOException {
        out.section("Document verification");
        if (values.isEmpty()) {
            out.empty("No document verification records.");
            return;
        }
        for (EbillDocumentSnapshot value : values) {
            out.card(printable(value.documentType()), List.of(
                    pair("Required", yesNo(value.required())), pair("Status", value.verificationStatus()),
                    pair("Verified by", value.verifiedBy()), pair("Verified at", value.verifiedAt())
            ));
        }
    }

    private void writePayments(PdfLayout out, List<EbillPaymentSnapshot> values) throws IOException {
        out.section("Payment details");
        if (values.isEmpty()) {
            out.empty("No payment records.");
            return;
        }
        for (EbillPaymentSnapshot value : values) {
            out.card(printable(value.paymentType()) + " payment", List.of(
                    pair("Amount", money(value.amount(), value.currency())), pair("Status", value.paymentStatus()),
                    pair("Method", value.paymentMethod()), pair("Transaction", value.transactionReference()),
                    pair("Due date", value.dueDate()), pair("Paid date", value.paidDate())
            ));
        }
    }

    private void writeEvents(PdfLayout out, List<EbillEventSnapshot> values) throws IOException {
        out.section("Shipment timeline");
        if (values.isEmpty()) {
            out.empty("No shipment events.");
            return;
        }
        for (EbillEventSnapshot value : values) {
            out.card(printable(value.eventType()), List.of(
                    pair("Event time", value.eventTime()), pair("Location", value.eventLocation()),
                    pair("Description", value.eventDescription())
            ));
        }
    }

    private void configureDocumentInformation(PDDocument document, EbillSnapshot snapshot) {
        PDDocumentInformation information = new PDDocumentInformation();
        information.setTitle("CargoSphere eBill " + printable(snapshot.ebillNumber()));
        information.setAuthor("CargoSphere");
        information.setSubject("Immutable electronic shipment bill");
        information.setKeywords("eBill, shipment, CargoSphere");
        document.setDocumentInformation(information);
    }

    private static Field pair(String label, Object value) {
        return new Field(label, printable(value));
    }

    private static String measure(BigDecimal value, String unit) {
        return value == null ? "N/A" : value.stripTrailingZeros().toPlainString() + " " + unit;
    }

    private static String money(BigDecimal amount, String currency) {
        return amount == null ? "N/A" : printable(currency) + " " + amount.setScale(2).toPlainString();
    }

    private static String ready(boolean value) {
        return value ? "Ready" : "Pending";
    }

    private static String yesNo(Boolean value) {
        return value == null ? "N/A" : value ? "Yes" : "No";
    }

    private static String printable(Object value) {
        if (value == null) return "N/A";
        String text = value instanceof TemporalAccessor ? value.toString().replace('T', ' ') : value.toString();
        return text.replaceAll("[^\\x20-\\x7E]", "?");
    }

    private record Field(String label, String value) { }

    private static final class PdfLayout {
        private static final float WIDTH = PDRectangle.A4.getWidth();
        private static final float HEIGHT = PDRectangle.A4.getHeight();
        private static final float MARGIN = 46F;
        private static final float CONTENT_WIDTH = WIDTH - (MARGIN * 2);
        private static final float BOTTOM = 62F;
        private static final PDColor INK = color(25, 34, 30);
        private static final PDColor MUTED = color(93, 107, 99);
        private static final PDColor GREEN = color(31, 107, 76);
        private static final PDColor PALE = color(241, 246, 242);
        private static final PDColor LINE = color(215, 224, 218);

        private final PDDocument document;
        private final String ebillNumber;
        private PDPageContentStream stream;
        private float y;
        private int pageNumber;

        private PdfLayout(PDDocument document, String ebillNumber) {
            this.document = document;
            this.ebillNumber = printable(ebillNumber);
        }

        void start() throws IOException { newPage(); }

        void title(String brand, String subtitle) throws IOException {
            text(brand, BOLD, 11, MARGIN, y, GREEN);
            text(subtitle, BOLD, 8, MARGIN, y - 15, MUTED);
            line(y - 29);
            y -= 54;
        }

        void hero(EbillSnapshot snapshot) throws IOException {
            ensure(112);
            fill(MARGIN, y - 96, CONTENT_WIDTH, 96, PALE);
            text("eBill", BOLD, 29, MARGIN + 20, y - 36, INK);
            text(snapshot.ebillNumber(), BOLD, 12, MARGIN + 20, y - 61, GREEN);
            text("Version " + printable(snapshot.ebillVersion()), REGULAR, 9, MARGIN + 20, y - 79, MUTED);
            text("Generated", BOLD, 8, MARGIN + 330, y - 29, MUTED);
            wrapped(printable(snapshot.generatedAt()), REGULAR, 9, MARGIN + 330, y - 46, 135, 12, INK);
            text("Generated by admin " + printable(snapshot.generatedBy()), REGULAR, 8, MARGIN + 330, y - 76, MUTED);
            y -= 119;
        }

        void section(String title) throws IOException {
            ensure(48);
            y -= 8;
            text(title.toUpperCase(), BOLD, 9, MARGIN, y, GREEN);
            line(y - 10);
            y -= 29;
        }

        void grid(List<Field> fields) throws IOException {
            for (int i = 0; i < fields.size(); i += 2) {
                ensure(41);
                field(fields.get(i), MARGIN);
                if (i + 1 < fields.size()) field(fields.get(i + 1), MARGIN + 255);
                y -= 41;
            }
            y -= 5;
        }

        void card(String heading, List<Field> fields) throws IOException {
            int rows = (fields.size() + 1) / 2;
            float height = 31 + rows * 31;
            ensure(height + 10);
            stroke(MARGIN, y - height, CONTENT_WIDTH, height, LINE);
            text(heading, BOLD, 10, MARGIN + 12, y - 19, INK);
            float rowY = y - 42;
            for (int i = 0; i < fields.size(); i += 2) {
                compactField(fields.get(i), MARGIN + 12, rowY);
                if (i + 1 < fields.size()) compactField(fields.get(i + 1), MARGIN + 261, rowY);
                rowY -= 31;
            }
            y -= height + 10;
        }

        void note(String label, String value) throws IOException {
            ensure(48);
            fill(MARGIN, y - 39, CONTENT_WIDTH, 39, PALE);
            text(label, BOLD, 8, MARGIN + 11, y - 15, MUTED);
            wrapped(value, REGULAR, 8, MARGIN + 110, y - 15, CONTENT_WIDTH - 121, 10, INK);
            y -= 51;
        }

        void empty(String value) throws IOException {
            ensure(34);
            text(value, REGULAR, 9, MARGIN, y, MUTED);
            y -= 32;
        }

        void declaration(String schemaVersion) throws IOException {
            section("Document declaration");
            ensure(62);
            wrapped("This PDF is a human-readable rendering of the immutable CargoSphere eBill snapshot. The eBill number and version identify the authoritative electronic record.", REGULAR, 8, MARGIN, y, CONTENT_WIDTH, 12, MUTED);
            y -= 48;
            text("Snapshot schema: " + printable(schemaVersion), REGULAR, 8, MARGIN, y, MUTED);
        }

        void finish() throws IOException { closePage(); }

        private void field(Field field, float x) throws IOException {
            text(field.label(), BOLD, 7, x, y, MUTED);
            wrapped(field.value(), REGULAR, 9, x, y - 14, 225, 11, INK);
        }

        private void compactField(Field field, float x, float rowY) throws IOException {
            text(field.label(), BOLD, 6.8F, x, rowY, MUTED);
            wrapped(field.value(), REGULAR, 8.3F, x, rowY - 12, 225, 10, INK);
        }

        private void ensure(float height) throws IOException {
            if (y - height < BOTTOM) {
                closePage();
                newPage();
            }
        }

        private void newPage() throws IOException {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            pageNumber++;
            y = HEIGHT - 48;
            if (pageNumber > 1) {
                text("CARGOSPHERE EBILL", BOLD, 8, MARGIN, y, GREEN);
                text(ebillNumber, REGULAR, 8, WIDTH - MARGIN - 150, y, MUTED);
                line(y - 11);
                y -= 34;
            }
        }

        private void closePage() throws IOException {
            if (stream == null) return;
            line(43);
            text("CargoSphere | Immutable electronic shipment bill", REGULAR, 7, MARGIN, 29, MUTED);
            text("Page " + pageNumber, REGULAR, 7, WIDTH - MARGIN - 35, 29, MUTED);
            stream.close();
            stream = null;
        }

        private void text(String value, PDFont font, float size, float x, float positionY, PDColor color) throws IOException {
            stream.beginText();
            stream.setNonStrokingColor(color);
            stream.setFont(font, size);
            stream.newLineAtOffset(x, positionY);
            stream.showText(printable(value));
            stream.endText();
        }

        private int wrapped(String value, PDFont font, float size, float x, float positionY,
                            float maxWidth, float leading, PDColor color) throws IOException {
            List<String> lines = wrap(printable(value), font, size, maxWidth);
            for (int i = 0; i < lines.size(); i++) text(lines.get(i), font, size, x, positionY - i * leading, color);
            return lines.size();
        }

        private List<String> wrap(String value, PDFont font, float size, float maxWidth) throws IOException {
            List<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String word : value.split("\\s+")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (font.getStringWidth(candidate) / 1000F * size <= maxWidth || line.length() == 0) {
                    line.setLength(0);
                    line.append(candidate);
                } else {
                    lines.add(line.toString());
                    line.setLength(0);
                    line.append(word);
                }
            }
            if (line.length() > 0) lines.add(line.toString());
            return lines.isEmpty() ? List.of("N/A") : lines;
        }

        private void line(float positionY) throws IOException {
            stream.setStrokingColor(LINE);
            stream.setLineWidth(0.7F);
            stream.moveTo(MARGIN, positionY);
            stream.lineTo(WIDTH - MARGIN, positionY);
            stream.stroke();
        }

        private void fill(float x, float positionY, float width, float height, PDColor color) throws IOException {
            stream.setNonStrokingColor(color);
            stream.addRect(x, positionY, width, height);
            stream.fill();
        }

        private void stroke(float x, float positionY, float width, float height, PDColor color) throws IOException {
            stream.setStrokingColor(color);
            stream.setLineWidth(0.7F);
            stream.addRect(x, positionY, width, height);
            stream.stroke();
        }

        private static PDColor color(int red, int green, int blue) {
            return new PDColor(new float[]{red / 255F, green / 255F, blue / 255F}, PDDeviceRGB.INSTANCE);
        }
    }
}
