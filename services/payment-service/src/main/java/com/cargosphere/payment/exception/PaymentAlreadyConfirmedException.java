package com.cargosphere.payment.exception;

public class PaymentAlreadyConfirmedException
        extends RuntimeException {

    public PaymentAlreadyConfirmedException(
            String message
    ) {
        super(message);
    }
}