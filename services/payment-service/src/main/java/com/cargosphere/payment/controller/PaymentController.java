package com.cargosphere.payment.controller;

import com.cargosphere.payment.config.OpenApiConfig;
import com.cargosphere.payment.dto.CreatePaymentRequest;
import com.cargosphere.payment.dto.ErrorResponse;
import com.cargosphere.payment.dto.PaymentResponse;
import com.cargosphere.payment.dto.RefundPaymentRequest;
import com.cargosphere.payment.dto.UpdatePaymentStatusRequest;
import com.cargosphere.payment.security.JwtUserIdExtractor;
import com.cargosphere.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Payments",
        description = "Payment creation, lookup, status and refund management"
)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtUserIdExtractor jwtUserIdExtractor;

    @Operation(
            summary = "Create a payment",
            description =
                    "Creates a payment for the authenticated user. "
                            + "Accessible to ROLE_ADMIN and ROLE_CLIENT."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Payment created successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = PaymentResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN or CLIENT role is required"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Transaction reference already exists",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid
            @RequestBody
            CreatePaymentRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal
            Jwt jwt
    ) {
        Long userId =
                jwtUserIdExtractor.extractUserId(jwt);

        PaymentResponse response =
                paymentService.createPayment(
                        request,
                        userId
                );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @Operation(
            summary = "Get all payments",
            description = "Returns every payment. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payments returned successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation =
                                                    PaymentResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>>
    getAllPayments() {

        return ResponseEntity.ok(
                paymentService.getAllPayments()
        );
    }

    @Operation(
            summary = "Get authenticated user's payments",
            description =
                    "Returns payments belonging to the authenticated user. "
                            + "Accessible to ROLE_ADMIN and ROLE_CLIENT."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User payments returned successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation =
                                                    PaymentResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN or CLIENT role is required"
            )
    })
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<PaymentResponse>>
    getMyPayments(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            Jwt jwt
    ) {
        Long userId =
                jwtUserIdExtractor.extractUserId(jwt);

        return ResponseEntity.ok(
                paymentService.getPaymentsByUserId(
                        userId
                )
        );
    }

    @Operation(
            summary = "Get payment by ID",
            description = "Returns one payment. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment returned successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = PaymentResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payment ID must be positive",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse>
    getPaymentById(
            @Parameter(
                    description = "Payment database ID",
                    example = "1",
                    required = true
            )
            @PathVariable
            @Positive(message = "Payment ID must be positive")
            Long paymentId
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentById(
                        paymentId
                )
        );
    }

    @Operation(
            summary = "Get payments by shipment ID",
            description =
                    "Returns all payments associated with a shipment. "
                            + "Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipment payments returned successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation =
                                                    PaymentResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Shipment ID must be positive",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            )
    })
    @GetMapping("/shipment/{shipmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>>
    getPaymentsByShipmentId(
            @Parameter(
                    description = "Shipment database ID",
                    example = "1001",
                    required = true
            )
            @PathVariable
            @Positive(message = "Shipment ID must be positive")
            Long shipmentId
    ) {
        return ResponseEntity.ok(
                paymentService
                        .getPaymentsByShipmentId(
                                shipmentId
                        )
        );
    }

    @Operation(
            summary = "Update payment status",
            description =
                    "Updates payment status, transaction reference and remarks. "
                            + "Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment status updated successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = PaymentResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Request validation failed or transition is invalid",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Transaction reference already exists",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PatchMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse>
    updatePaymentStatus(
            @Parameter(
                    description = "Payment database ID",
                    example = "1",
                    required = true
            )
            @PathVariable
            @Positive(message = "Payment ID must be positive")
            Long paymentId,

            @Valid
            @RequestBody
            UpdatePaymentStatusRequest request
    ) {
        return ResponseEntity.ok(
                paymentService.updatePaymentStatus(
                        paymentId,
                        request
                )
        );
    }

    @Operation(
            summary = "Refund a payment",
            description =
                    "Refunds an eligible payment and records the refund reason. "
                            + "Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment refunded successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = PaymentResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Refund request is invalid or payment cannot be refunded",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse>
    refundPayment(
            @Parameter(
                    description = "Payment database ID",
                    example = "1",
                    required = true
            )
            @PathVariable
            @Positive(message = "Payment ID must be positive")
            Long paymentId,

            @Valid
            @RequestBody
            RefundPaymentRequest request
    ) {
        return ResponseEntity.ok(
                paymentService.refundPayment(
                        paymentId,
                        request
                )
        );
    }
}
