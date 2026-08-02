package com.cargosphere.shipment.service.support;

import com.cargosphere.shipment.dto.ebill.snapshot.EbillSnapshot;
import com.cargosphere.shipment.exception.EbillSnapshotSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class EbillSnapshotJsonService {

    private final ObjectMapper objectMapper;

    public EbillSnapshotJsonService(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public String serialize(
            EbillSnapshot snapshot
    ) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new EbillSnapshotSerializationException(
                    "Failed to serialize eBill snapshot",
                    exception
            );
        }
    }

    public EbillSnapshot deserialize(
            String snapshotJson
    ) {
        try {
            return objectMapper.readValue(
                    snapshotJson,
                    EbillSnapshot.class
            );
        } catch (JsonProcessingException exception) {
            throw new EbillSnapshotSerializationException(
                    "Failed to deserialize eBill snapshot",
                    exception
            );
        }
    }
}
