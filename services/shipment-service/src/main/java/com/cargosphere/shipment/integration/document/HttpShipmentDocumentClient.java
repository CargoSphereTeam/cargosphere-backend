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
            throw new DownstreamServiceException(
                    SERVICE_NAME,
                    "Document-service rejected document lookup for shipment ID "
                            + shipmentId
                            + " with status "
                            + exception.getStatusCode(),
                    exception
            );

        } catch (RestClientException exception) {
            throw new DownstreamServiceException(
                    SERVICE_NAME,
                    "Document-service is unavailable while checking shipment ID "
                            + shipmentId,
                    exception
            );
        }
    }
}
