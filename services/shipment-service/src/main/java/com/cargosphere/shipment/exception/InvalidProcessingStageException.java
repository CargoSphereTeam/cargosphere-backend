package com.cargosphere.shipment.exception;

import com.cargosphere.shipment.entity.enums.ProcessingStage;
import lombok.Getter;

@Getter
public class InvalidProcessingStageException extends RuntimeException {

    private static final String ERROR_CODE =
            "INVALID_PROCESSING_STAGE";

    private final String code;

    private final ProcessingStage currentStage;

    public InvalidProcessingStageException(
            Long shipmentId,
            ProcessingStage expectedStage,
            ProcessingStage currentStage
    ) {
        super(buildMessage(
                shipmentId,
                expectedStage,
                currentStage
        ));

        this.code = ERROR_CODE;
        this.currentStage = currentStage;
    }

    private static String buildMessage(
            Long shipmentId,
            ProcessingStage expectedStage,
            ProcessingStage currentStage
    ) {
        return "Shipment "
                + shipmentId
                + " must be in processing stage "
                + expectedStage
                + ", but current stage is "
                + currentStage;
    }
}