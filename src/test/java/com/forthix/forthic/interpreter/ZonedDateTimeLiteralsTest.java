package com.forthix.forthic.interpreter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ZonedDateTimeLiteralsTest {
    private LiteralHandler handler;

    @BeforeEach
    void setUp() {
        handler = Literals.toZonedDateTime(ZoneId.of("America/New_York"));
    }

    // Literal Parsing Tests

    @Test
    void testParseUTCDatetimeWithZSuffix() {
        ZonedDateTime result = (ZonedDateTime) handler.handle("2025-05-24T10:15:00Z");

        assertNotNull(result);
        assertEquals("UTC", result.getZone().getId());
    }

    @Test
    void testParseIANATimezoneWithBracketNotation() {
        ZonedDateTime result = (ZonedDateTime) handler.handle("2025-05-20T08:00:00[America/Los_Angeles]");

        assertNotNull(result);
        assertEquals("America/Los_Angeles", result.getZone().getId());
        assertEquals(8, result.getHour());
    }

    @Test
    void testParseDatetimeWithOffsetAndIANATimezone() {
        ZonedDateTime result = (ZonedDateTime) handler.handle("2025-05-20T08:00:00-07:00[America/Los_Angeles]");

        assertNotNull(result);
        assertEquals("America/Los_Angeles", result.getZone().getId());
        assertEquals(8, result.getHour());
    }

    @Test
    void testParseDatetimeWithOffsetOnly() {
        ZonedDateTime result = (ZonedDateTime) handler.handle("2025-05-24T10:15:00-05:00");

        assertNotNull(result);
        assertEquals(10, result.getHour());
    }

    @Test
    void testParseDatetimeWithoutTimezoneUsesDefault() {
        LiteralHandler laHandler = Literals.toZonedDateTime(ZoneId.of("America/Los_Angeles"));
        ZonedDateTime result = (ZonedDateTime) laHandler.handle("2025-05-24T10:15:00");

        assertNotNull(result);
        assertEquals("America/Los_Angeles", result.getZone().getId());
        assertEquals(10, result.getHour());
    }

    @Test
    void testParseVariousIANATimezones() {
        LiteralHandler utcHandler = Literals.toZonedDateTime(ZoneId.of("UTC"));

        // Europe/London
        ZonedDateTime result1 = (ZonedDateTime) utcHandler.handle("2025-05-20T14:30:00[Europe/London]");
        assertNotNull(result1);
        assertEquals("Europe/London", result1.getZone().getId());

        // Asia/Tokyo
        ZonedDateTime result2 = (ZonedDateTime) utcHandler.handle("2025-05-20T09:00:00[Asia/Tokyo]");
        assertNotNull(result2);
        assertEquals("Asia/Tokyo", result2.getZone().getId());

        // Australia/Sydney
        ZonedDateTime result3 = (ZonedDateTime) utcHandler.handle("2025-05-20T18:00:00[Australia/Sydney]");
        assertNotNull(result3);
        assertEquals("Australia/Sydney", result3.getZone().getId());
    }

    @Test
    void testReturnsNullForInvalidIANATimezone() {
        Object result = handler.handle("2025-05-20T08:00:00[Invalid/Timezone]");

        assertNull(result);
    }

    @Test
    void testReturnsNullForStringsWithoutT() {
        assertNull(handler.handle("2025-05-20"));
        assertNull(handler.handle("regular-word"));
        assertNull(handler.handle("08:00:00"));
    }

    @Test
    void testReturnsNullForMalformedDatetimeStrings() {
        assertNull(handler.handle("2025-13-45T10:15:00"));  // Invalid month/day
        assertNull(handler.handle("not-a-datetime[America/Los_Angeles]"));
        assertNull(handler.handle("2025-05-20T25:00:00"));  // Invalid hour
    }

    @Test
    void testReturnsNullForBracketsWithoutDatetime() {
        assertNull(handler.handle("[America/Los_Angeles]"));
        assertNull(handler.handle("word[bracket]"));
    }

    @Test
    void testParseDatetimeWithSeconds() {
        ZonedDateTime result = (ZonedDateTime) handler.handle("2025-05-20T08:30:45[America/Los_Angeles]");

        assertNotNull(result);
        assertEquals("America/Los_Angeles", result.getZone().getId());
        assertEquals(8, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
    }

    @Test
    void testParseDatetimeWithMilliseconds() {
        ZonedDateTime result = (ZonedDateTime) handler.handle("2025-05-20T08:30:45.123[America/Los_Angeles]");

        assertNotNull(result);
        assertEquals("America/Los_Angeles", result.getZone().getId());
        assertEquals(8, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
    }

    @Test
    void testParseUTCDatetimeWithBrackets() {
        ZonedDateTime result = (ZonedDateTime) handler.handle("2025-05-20T08:00:00Z[UTC]");

        assertNotNull(result);
        assertEquals("UTC", result.getZone().getId());
    }

    @Test
    void testPreservesInstantInTime() {
        ZonedDateTime result = (ZonedDateTime) handler.handle("2025-05-20T08:00:00[America/Los_Angeles]");

        assertNotNull(result);

        // Convert to UTC to verify it's the same instant
        ZonedDateTime utcTime = result.withZoneSameInstant(ZoneId.of("UTC"));

        // 8 AM PDT (UTC-7) = 3 PM UTC
        assertEquals(15, utcTime.getHour());  // 8 + 7 = 15 (3 PM)
    }
}
