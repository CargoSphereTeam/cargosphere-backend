package com.cargosphere.shipment.integration.payment;

import com.cargosphere.shipment.exception.DownstreamServiceException;
import com.cargosphere.shipment.security.CurrentJwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
public class HttpShipmentPaymentClient
        implements ShipmentPaymentClient {

    private static final String SERVICE_NAME =
            "payment-service";

    private final RestClient restClient;

    private final CurrentJwtTokenProvider tokenProvider;

    public HttpShipmentPaymentClient(
            @Qualifier("paymentRestClient")
            RestClient restClient,
            CurrentJwtTokenProvider tokenProvider
    ) {
        this.restClient = restClient;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public List<ShipmentPaymentResponse>
    getPaymentsByShipmentId(Long shipmentId) {

        String token = tokenProvider.getTokenValue();

        try {
            List<ShipmentPaymentResponse> response =
                    restClient
                            .get()
                            .uri(
                                    "/api/payments/shipment/{shipmentId}",
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
                    "payment lookup",
                    exception
            );

        } catch (RestClientException exception) {
            throw createUnavailableException(
                    shipmentId,
                    "loading payments",
                    exception
            );
        }
    }

    @Override
    public ShipmentPaymentSummaryResponse
    getPaymentSummary(Long shipmentId) {

        String token = tokenProvider.getTokenValue();

        try {
            return restClient
                    .get()
                    .uri(
                            "/api/payments/shipments/"
                                    + "{shipmentId}/payment-summary",
                            shipmentId
                    )
                    .headers(headers ->
                            headers.setBearerAuth(token)
                    )
                    .retrieve()
                    .body(
                            ShipmentPaymentSummaryResponse.class
                    );

        } catch (RestClientResponseException exception) {
            if (
                    exception.getStatusCode()
                            == HttpStatus.NOT_FOUND
            ) {
                return null;
            }

            throw createRejectedException(
                    shipmentId,
                    "payment-summary lookup",
                    exception
            );

        } catch (RestClientException exception) {
            throw createUnavailableException(
                    shipmentId,
                    "loading payment summary",
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
                "Payment-service rejected "
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
                "Payment-service is unavailable while "
                        + operation
                        + " for shipment ID "
                        + shipmentId,
                exception
        );
    }
}