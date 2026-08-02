package com.cargosphere.shipment.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerDownstreamServiceTest {

    @Test
    void shouldReturnServiceUnavailableForDownstreamFailure() {
        GlobalExceptionHandler handler =
                new GlobalExceptionHandler();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn(
                        "/api/admin/shipments/101/processing/continue"
                );

        DownstreamServiceException exception =
                new DownstreamServiceException(
                        "payment-service",
                        "Payment-service is unavailable while checking shipment ID 101"
                );

        ResponseEntity<ApiErrorResponse> response =
                handler.handleDownstreamServiceException(
                        exception,
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(
                        HttpStatus.SERVICE_UNAVAILABLE
                );

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().getStatus())
                .isEqualTo(
                        HttpStatus.SERVICE_UNAVAILABLE.value()
                );

        assertThat(response.getBody().getError())
                .isEqualTo(
                        HttpStatus.SERVICE_UNAVAILABLE
                                .getReasonPhrase()
                );

        assertThat(response.getBody().getCode())
                .isEqualTo(
                        "DOWNSTREAM_SERVICE_UNAVAILABLE"
                );

        assertThat(response.getBody().getMessage())
                .contains("Payment-service")
                .contains("101");

        assertThat(response.getBody().getPath())
                .isEqualTo(
                        "/api/admin/shipments/101/processing/continue"
                );

        assertThat(exception.getServiceName())
                .isEqualTo("payment-service");
    }
}
