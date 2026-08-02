package com.cargosphere.shipment.integration.auth;

public interface AuthUserClient {

    AuthUserResponse getUserById(Long userId);
}
