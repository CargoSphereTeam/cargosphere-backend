package com.cargosphere.shipment.exception;

import com.cargosphere.shipment.entity.enums.ProcessingStage;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerProcessingStageTest {

    private GlobalExceptionHandler handler;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn(
                        "/api/admin/shipments/10/cargo-verification"
                );
    }

    @Test
    void shouldReturnConflictForInvalidProcessingStage() {
        InvalidProcessingStageException exception =
                new InvalidProcessingStageException(
                        10L,
                        ProcessingStage.CARGO_VERIFICATION,
                        ProcessingStage.CONTAINER_ALLOCATION
                );

        ResponseEntity<ApiErrorResponse> response =
                handler.handleInvalidProcessingStageException(
                        exception,
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().getStatus())
                .isEqualTo(HttpStatus.CONFLICT.value());

        assertThat(response.getBody().getError())
                .isEqualTo(HttpStatus.CONFLICT.getReasonPhrase());

        assertThat(response.getBody().getCode())
                .isEqualTo("INVALID_PROCESSING_STAGE");

        assertThat(response.getBody().getCurrentStage())
                .isEqualTo(
                        ProcessingStage.CONTAINER_ALLOCATION
                );

        assertThat(response.getBody().getPath())
                .isEqualTo(
                        "/api/admin/shipments/10/cargo-verification"
                );

        assertThat(response.getBody().getMessage())
                .contains("Shipment 10")
                .contains("CARGO_VERIFICATION")
                .contains("CONTAINER_ALLOCATION");
    }

    @Test
    void shouldPreserveExceptionStageInformation() {
        InvalidProcessingStageException exception =
                new InvalidProcessingStageException(
                        25L,
                        ProcessingStage.CARGO_VERIFICATION,
                        ProcessingStage.DOCUMENT_VERIFICATION
                );

        assertThat(exception.getCode())
                .isEqualTo("INVALID_PROCESSING_STAGE");

        assertThat(exception.getCurrentStage())
                .isEqualTo(
                        ProcessingStage.DOCUMENT_VERIFICATION
                );

        assertThat(exception.getMessage())
                .contains("Shipment 25")
                .contains("CARGO_VERIFICATION")
                .contains("DOCUMENT_VERIFICATION");
    }
}