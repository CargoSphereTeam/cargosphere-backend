package com.cargosphere.payment.exception;

public class PaymentConfirmationNotAllowedException
        extends RuntimeException {

    public PaymentConfirmationNotAllowedException(
            String message
    ) {
        super(message);
    }
}