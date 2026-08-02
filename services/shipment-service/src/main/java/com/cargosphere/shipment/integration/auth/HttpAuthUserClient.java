package com.cargosphere.shipment.integration.auth;

import com.cargosphere.shipment.exception.DownstreamServiceException;
import com.cargosphere.shipment.security.CurrentJwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class HttpAuthUserClient
        implements AuthUserClient {

    private static final String SERVICE_NAME =
            "auth-service";

    private final RestClient restClient;

    private final CurrentJwtTokenProvider tokenProvider;

    public HttpAuthUserClient(
            @Qualifier("authRestClient")
            RestClient restClient,
            CurrentJwtTokenProvider tokenProvider
    ) {
        this.restClient = restClient;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthUserResponse getUserById(
            Long userId
    ) {
        String token = tokenProvider.getTokenValue();

        try {
            AuthUserResponse response =
                    restClient
                            .get()
                            .uri(
                                    "/api/auth/users/{userId}",
                                    userId
                            )
                            .headers(headers ->
                                    headers.setBearerAuth(token)
                            )
                            .retrieve()
                            .body(AuthUserResponse.class);

            if (response == null) {
                throw new DownstreamServiceException(
                        SERVICE_NAME,
                        "Auth-service returned an empty response "
                                + "for user ID "
                                + userId,
                        new IllegalStateException(
                                "Empty auth-service response"
                        )
                );
            }

            return response;

        } catch (DownstreamServiceException exception) {
            throw exception;

        } catch (RestClientResponseException exception) {
            throw new DownstreamServiceException(
                    SERVICE_NAME,
                    "Auth-service rejected user lookup for user ID "
                            + userId
                            + " with status "
                            + exception.getStatusCode(),
                    exception
            );

        } catch (RestClientException exception) {
            throw new DownstreamServiceException(
                    SERVICE_NAME,
                    "Auth-service is unavailable while looking up "
                            + "user ID "
                            + userId,
                    exception
            );
        }
    }
}
