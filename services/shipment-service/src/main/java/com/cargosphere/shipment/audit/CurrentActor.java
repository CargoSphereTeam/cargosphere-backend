package com.cargosphere.shipment.audit;

public record CurrentActor(
        Long userId,
        String role
) {

    public static CurrentActor anonymous() {
        return new CurrentActor(null, null);
    }
}