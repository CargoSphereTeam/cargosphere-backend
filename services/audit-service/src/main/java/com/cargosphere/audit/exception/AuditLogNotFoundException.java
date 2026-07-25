package com.cargosphere.audit.exception;

public class AuditLogNotFoundException
        extends RuntimeException {

    public AuditLogNotFoundException(String message) {
        super(message);
    }
}