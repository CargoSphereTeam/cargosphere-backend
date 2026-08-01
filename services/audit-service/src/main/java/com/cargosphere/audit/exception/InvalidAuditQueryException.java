package com.cargosphere.audit.exception;

public class InvalidAuditQueryException
        extends RuntimeException {

    public InvalidAuditQueryException(String message) {
        super(message);
    }
}