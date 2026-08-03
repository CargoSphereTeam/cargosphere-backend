package com.cargosphere.shipment.dto.ebill;

public record EbillPdfDocument(
        String fileName,
        byte[] content
) {

    public EbillPdfDocument {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException(
                    "PDF file name must not be blank"
            );
        }

        if (content == null || content.length == 0) {
            throw new IllegalArgumentException(
                    "PDF content must not be empty"
            );
        }

        content = content.clone();
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
