package com.cargosphere.shipment.integration.payment;

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
            throw new DownstreamServiceException(
                    SERVICE_NAME,
                    "Payment-service rejected payment lookup for shipment ID "
                            + shipmentId
                            + " with status "
                            + exception.getStatusCode(),
                    exception
            );

        } catch (RestClientException exception) {
            throw new DownstreamServiceException(
                    SERVICE_NAME,
                    "Payment-service is unavailable while checking shipment ID "
                            + shipmentId,
                    exception
            );
        }
    }
}
