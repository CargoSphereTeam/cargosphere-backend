package com.cargosphere.shipment.service.support;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EbillNumberGeneratorTest {

    private final EbillNumberGenerator generator =
            new EbillNumberGenerator();

    @Test
    void shouldGenerateEbillNumberUsingUtcDate() {
        OffsetDateTime generatedAt =
                OffsetDateTime.of(
                        2026,
                        8,
                        3,
                        1,
                        0,
                        0,
                        0,
                        ZoneOffset.ofHoursMinutes(
                                5,
                                30
                        )
                );

        String ebillNumber =
                generator.generate(generatedAt);

        assertThat(ebillNumber)
                .matches(
                        "EBL-20260802-[A-F0-9]{8}"
                );
    }

    @Test
    void shouldGenerateDifferentEbillNumbers() {
        OffsetDateTime generatedAt =
                OffsetDateTime.of(
                        2026,
                        8,
                        3,
                        9,
                        0,
                        0,
                        0,
                        ZoneOffset.UTC
                );

        String first =
                generator.generate(generatedAt);

        String second =
                generator.generate(generatedAt);

        assertThat(first)
                .isNotEqualTo(second);
    }

    @Test
    void shouldRejectNullGeneratedTimestamp() {
        assertThatThrownBy(
                () -> generator.generate(null)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Generated timestamp must not be null"
                );
    }
}
