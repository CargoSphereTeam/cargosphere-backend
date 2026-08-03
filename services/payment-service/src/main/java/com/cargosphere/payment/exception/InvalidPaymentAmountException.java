package com.cargosphere.payment.exception;

public class InvalidPaymentAmountException
        extends RuntimeException {

    public InvalidPaymentAmountException(
            String message
    ) {
        super(message);
    }
}