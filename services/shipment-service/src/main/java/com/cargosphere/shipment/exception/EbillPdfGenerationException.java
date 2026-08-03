package com.cargosphere.shipment.exception;

public class EbillPdfGenerationException
        extends RuntimeException {

    public EbillPdfGenerationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
