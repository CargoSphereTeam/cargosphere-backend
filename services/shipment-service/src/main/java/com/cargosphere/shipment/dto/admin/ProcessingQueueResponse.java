package com.cargosphere.shipment.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ProcessingQueueResponse",
        description =
                "Paginated administrator shipment-processing queue"
)
public class ProcessingQueueResponse {

    @Schema(
            description = "Shipments available on the current page"
    )
    private List<ProcessingQueueItemResponse> items;

    @Schema(
            description = "Current zero-based page number",
            example = "0"
    )
    private int page;

    @Schema(
            description = "Maximum number of shipments on the page",
            example = "20"
    )
    private int size;

    @Schema(
            description = "Total number of matching shipments",
            example = "45"
    )
    private long totalElements;

    @Schema(
            description = "Total number of available pages",
            example = "3"
    )
    private int totalPages;

    @Schema(
            description = "Whether this is the first page",
            example = "true"
    )
    private boolean first;

    @Schema(
            description = "Whether this is the last page",
            example = "false"
    )
    private boolean last;

    @Schema(
            description = "Whether the current page has no shipments",
            example = "false"
    )
    private boolean empty;
}