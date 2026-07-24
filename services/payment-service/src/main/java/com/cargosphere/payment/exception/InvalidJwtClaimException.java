package com.cargosphere.payment.exception;

public class InvalidJwtClaimException
        extends RuntimeException {

    public InvalidJwtClaimException(String message) {
        super(message);
    }
}