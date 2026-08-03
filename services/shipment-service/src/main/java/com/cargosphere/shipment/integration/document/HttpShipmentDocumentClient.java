package com.cargosphere.shipment.integration.document;

import com.cargosphere.shipment.exception.DownstreamServiceException;
import com.cargosphere.shipment.security.CurrentJwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
public class HttpShipmentDocumentClient
        implements ShipmentDocumentClient {

    private static final String SERVICE_NAME =
            "document-service";

    private final RestClient restClient;

    private final CurrentJwtTokenProvider tokenProvider;

    public HttpShipmentDocumentClient(
            @Qualifier("documentRestClient")
            RestClient restClient,
            CurrentJwtTokenProvider tokenProvider
    ) {
        this.restClient = restClient;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public List<ShipmentDocumentResponse>
    getDocumentsByShipmentId(Long shipmentId) {

        String token = tokenProvider.getTokenValue();

        try {
            List<ShipmentDocumentResponse> response =
                    restClient
                            .get()
                            .uri(
                                    "/api/documents/shipment/{shipmentId}",
                                    shipmentId
                            )
                            .headers(headers ->
                                    headers.setBearerAuth(token)
                            )
                            .retrieve()
                            .body(
                                    new ParameterizedTypeReference<>() {
                                    }
                            );

            if (response == null) {
                return List.of();
            }

            return List.copyOf(response);

        } catch (RestClientResponseException exception) {
            throw createRejectedException(
                    shipmentId,
                    "document lookup",
                    exception
            );

        } catch (RestClientException exception) {
            throw createUnavailableException(
                    shipmentId,
                    "loading documents",
                    exception
            );
        }
    }

    @Override
    public ShipmentDocumentReadinessResponse
    getDocumentReadiness(Long shipmentId) {

        String token = tokenProvider.getTokenValue();

        try {
            ShipmentDocumentReadinessResponse response =
                    restClient
                            .get()
                            .uri(
                                    "/api/documents/shipment/"
                                            + "{shipmentId}/readiness",
                                    shipmentId
                            )
                            .headers(headers ->
                                    headers.setBearerAuth(token)
                            )
                            .retrieve()
                            .body(
                                    ShipmentDocumentReadinessResponse.class
                            );

            if (response == null) {
                throw new DownstreamServiceException(
                        SERVICE_NAME,
                        "Document-service returned an empty readiness "
                                + "response for shipment ID "
                                + shipmentId
                );
            }

            return response;

        } catch (RestClientResponseException exception) {
            throw createRejectedException(
                    shipmentId,
                    "readiness lookup",
                    exception
            );

        } catch (DownstreamServiceException exception) {
            throw exception;

        } catch (RestClientException exception) {
            throw createUnavailableException(
                    shipmentId,
                    "checking document readiness",
                    exception
            );
        }
    }

    private DownstreamServiceException
    createRejectedException(
            Long shipmentId,
            String operation,
            RestClientResponseException exception
    ) {
        return new DownstreamServiceException(
                SERVICE_NAME,
                "Document-service rejected "
                        + operation
                        + " for shipment ID "
                        + shipmentId
                        + " with status "
                        + exception.getStatusCode(),
                exception
        );
    }

    private DownstreamServiceException
    createUnavailableException(
            Long shipmentId,
            String operation,
            RestClientException exception
    ) {
        return new DownstreamServiceException(
                SERVICE_NAME,
                "Document-service is unavailable while "
                        + operation
                        + " for shipment ID "
                        + shipmentId,
                exception
        );
    }
}