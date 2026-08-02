package com.cargosphere.shipment.exception;

public class EbillSnapshotSerializationException
        extends RuntimeException {

    public EbillSnapshotSerializationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
