package com.cargosphere.shipment.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Action requested for the cargo verification workflow")
public enum CargoVerificationAction {

    SAVE_DRAFT,

    CONFIRM_AND_CONTINUE
}