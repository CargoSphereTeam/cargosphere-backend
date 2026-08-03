package com.cargosphere.shipment.exception;

import lombok.Getter;

@Getter
public class DownstreamServiceException
        extends RuntimeException {

    private final String serviceName;

    public DownstreamServiceException(
            String serviceName,
            String message
    ) {
        super(message);
        this.serviceName = serviceName;
    }

    public DownstreamServiceException(
            String serviceName,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.serviceName = serviceName;
    }
}
