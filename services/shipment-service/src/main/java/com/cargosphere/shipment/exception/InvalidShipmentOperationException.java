package com.cargosphere.shipment.exception;

public class InvalidShipmentOperationException extends RuntimeException {

    public InvalidShipmentOperationException(String message) {
        super(message);
    }
}