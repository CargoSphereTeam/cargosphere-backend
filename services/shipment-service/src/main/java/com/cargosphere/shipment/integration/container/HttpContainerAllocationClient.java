package com.cargosphere.shipment.integration.container;

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
public class HttpContainerAllocationClient
        implements ContainerAllocationClient {

    private static final String SERVICE_NAME =
            "container-service";

    private final RestClient restClient;

    private final CurrentJwtTokenProvider tokenProvider;

    public HttpContainerAllocationClient(
            @Qualifier("containerRestClient")
            RestClient restClient,
            CurrentJwtTokenProvider tokenProvider
    ) {
        this.restClient = restClient;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public List<ContainerAllocationResponse>
    getAllocationsByShipmentId(Long shipmentId) {

        String token = tokenProvider.getTokenValue();

        try {
            List<ContainerAllocationResponse> response =
                    restClient
                            .get()
                            .uri(
                                    "/api/container-allocations/shipment/{shipmentId}",
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
                    "Container-service rejected allocation lookup for shipment ID "
                            + shipmentId
                            + " with status "
                            + exception.getStatusCode(),
                    exception
            );

        } catch (RestClientException exception) {
            throw new DownstreamServiceException(
                    SERVICE_NAME,
                    "Container-service is unavailable while checking shipment ID "
                            + shipmentId,
                    exception
            );
        }
    }
}
