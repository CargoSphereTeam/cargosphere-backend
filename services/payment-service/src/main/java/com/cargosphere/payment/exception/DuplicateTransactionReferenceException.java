package com.cargosphere.payment.exception;

public class DuplicateTransactionReferenceException
        extends RuntimeException {

    public DuplicateTransactionReferenceException(
            String message
    ) {
        super(message);
    }
}