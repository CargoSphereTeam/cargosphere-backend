package com.cargosphere.shipment.service.support;

import com.cargosphere.shipment.dto.ebill.snapshot.EbillReadinessSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillSnapshot;
import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.exception.EbillSnapshotSerializationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EbillSnapshotJsonServiceTest {

    private final ObjectMapper objectMapper =
            JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .build();

    private final EbillSnapshotJsonService service =
            new EbillSnapshotJsonService(objectMapper);

    @Test
    void shouldSerializeAndDeserializeSnapshot() {
        OffsetDateTime generatedAt =
                OffsetDateTime.of(
                        2026,
                        8,
                        3,
                        1,
                        30,
                        0,
                        0,
                        ZoneOffset.UTC
                );

        EbillSnapshot snapshot =
                new EbillSnapshot(
                        "1.0",
                        "EBL-20260803-12345678",
                        1,
                        generatedAt,
                        5L,
                        null,
                        null,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        new EbillReadinessSnapshot(
                                ProcessingStage.READY_FOR_EBILL,
                                true,
                                true,
                                true,
                                true,
                                true,
                                List.of()
                        )
                );

        String snapshotJson =
                service.serialize(snapshot);

        EbillSnapshot restoredSnapshot =
                service.deserialize(snapshotJson);

        assertThat(snapshotJson)
                .contains(
                        "\"ebillNumber\":\"EBL-20260803-12345678\""
                );

        assertThat(restoredSnapshot)
                .isEqualTo(snapshot);
    }

    @Test
    void shouldWrapInvalidSnapshotJsonFailure() {
        assertThatThrownBy(
                () -> service.deserialize(
                        "{invalid-json"
                )
        )
                .isInstanceOf(
                        EbillSnapshotSerializationException.class
                )
                .hasMessage(
                        "Failed to deserialize eBill snapshot"
                )
                .hasCauseInstanceOf(
                        com.fasterxml.jackson.core
                                .JsonProcessingException.class
                );
    }
}
