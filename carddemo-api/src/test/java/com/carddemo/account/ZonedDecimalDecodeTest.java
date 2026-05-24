package com.carddemo.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-function tests for zoned-decimal sign-overpunch decoding (PIC S9(n)V99).
 * Reference: IBM Enterprise COBOL Programming Guide, "ZONED-DECIMAL data items".
 */
class ZonedDecimalDecodeTest {

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @DisplayName("S9(10)V99 zoned decimal decodes per the COBOL overpunch table")
    @CsvSource({
            // 12 chars total: 11 plain digits + 1 overpunched last digit. V99 -> /100.
            // Positive values (last byte = digit + '{'..'I')
            "00000001940{ , 194.00",
            "00000020200{ , 2020.00",
            "00000000000{ , 0.00",
            "00000000000A , 0.01",
            "00000000000E , 0.05",
            "00000000000I , 0.09",
            // Negative values ('}' and 'J'..'R')
            "00000000001} , -0.10",
            "00000000010J , -1.01",
            "00000000010R , -1.09",
            // Plain ASCII digit accepted as positive
            "000000000015 , 0.15"
    })
    void overpunchDecoding(String raw, BigDecimal expected) {
        assertThat(CardDemoDataLoader.decodeS9V99(raw)).isEqualByComparingTo(expected);
    }

    @ParameterizedTest(name = "[{index}] S9(4)V99 {0} -> {1}")
    @DisplayName("disclosure rate S9(4)V99 (6 chars) decodes correctly")
    @CsvSource({
            "00150{,  15.00",
            "00250{,  25.00",
            "00000{,   0.00",
            // 00125F -> digits 001256 (F = +6) -> /100 -> 12.56
            "00125F,  12.56"
    })
    void rateDecoding(String raw, BigDecimal expected) {
        assertThat(CardDemoDataLoader.decodeS9V99(raw, 4)).isEqualByComparingTo(expected);
    }
}
