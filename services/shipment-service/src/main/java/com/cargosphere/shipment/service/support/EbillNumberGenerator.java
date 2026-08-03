package com.cargosphere.shipment.service.support;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Component
public class EbillNumberGenerator {

    private static final String PREFIX = "EBL-";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.BASIC_ISO_DATE;

    public String generate(
            OffsetDateTime generatedAt
    ) {
        if (generatedAt == null) {
            throw new IllegalArgumentException(
                    "Generated timestamp must not be null"
            );
        }

        String datePart =
                generatedAt
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .toLocalDate()
                        .format(DATE_FORMATTER);

        String randomPart =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase(Locale.ROOT);

        return PREFIX
                + datePart
                + "-"
                + randomPart;
    }
}
