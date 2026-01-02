package com.forthix.forthic.interpreter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Standard literal handlers for Forthic.
 */
public class Literals {

    /**
     * Parse boolean literals: TRUE, FALSE
     */
    public static Object toBool(String str) {
        if ("TRUE".equals(str)) return true;
        if ("FALSE".equals(str)) return false;
        return null;
    }

    /**
     * Parse float literals: 3.14, -2.5, 0.0
     * Must contain a decimal point
     */
    public static Object toFloat(String str) {
        if (!str.contains(".")) return null;
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse integer literals: 42, -10, 0
     * Must not contain a decimal point
     */
    public static Object toInt(String str) {
        if (str.contains(".")) return null;
        try {
            int result = Integer.parseInt(str);
            // Verify it's actually an integer string (not "42abc")
            if (!Integer.toString(result).equals(str)) return null;
            return result;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Create a zoned datetime literal handler with timezone support.
     * Parses ISO 8601 datetime with IANA timezone bracket notation:
     * - 2025-05-20T08:00:00[America/Los_Angeles]  (IANA timezone)
     * - 2025-05-20T08:00:00-07:00[America/Los_Angeles]  (offset + IANA)
     * - 2025-05-24T10:15:00Z  (UTC)
     * - 2025-05-24T10:15:00-05:00  (offset only)
     * - 2025-05-24T10:15:00  (uses default timezone)
     */
    public static LiteralHandler toZonedDateTime(ZoneId defaultTimezone) {
        return str -> {
            if (!str.contains("T")) return null;

            try {
                // Extract IANA timezone from brackets if present
                Pattern bracketPattern = Pattern.compile("\\[([^\\]]+)\\]$");
                Matcher bracketMatcher = bracketPattern.matcher(str);

                if (bracketMatcher.find()) {
                    // Extract IANA timezone name from brackets
                    String tzName = bracketMatcher.group(1);

                    // Validate timezone identifier
                    ZoneId tz;
                    try {
                        tz = ZoneId.of(tzName);
                    } catch (Exception e) {
                        return null;  // Invalid timezone
                    }

                    // Extract datetime string (before bracket)
                    String datetimeStr = str.substring(0, bracketMatcher.start());

                    // Handle Z suffix - convert to +00:00
                    if (datetimeStr.endsWith("Z")) {
                        datetimeStr = datetimeStr.substring(0, datetimeStr.length() - 1) + "+00:00";
                    }

                    // Parse datetime (may have offset)
                    ZonedDateTime zdt;
                    try {
                        // Try ISO format with offset
                        zdt = ZonedDateTime.parse(datetimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        // Convert to specified timezone
                        return zdt.withZoneSameInstant(tz);
                    } catch (DateTimeParseException e) {
                        try {
                            // Try simple format without offset - parse in the target timezone
                            return ZonedDateTime.parse(datetimeStr,
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(tz));
                        } catch (DateTimeParseException e2) {
                            return null;
                        }
                    }
                }

                // No brackets - handle as before

                // Handle explicit UTC (Z suffix)
                if (str.endsWith("Z")) {
                    return ZonedDateTime.parse(str, DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC")));
                }

                // Handle explicit timezone offset (+05:00, -05:00)
                Pattern offsetPattern = Pattern.compile("[+-]\\d{2}:\\d{2}$");
                if (offsetPattern.matcher(str).find()) {
                    return ZonedDateTime.parse(str, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                }

                // No timezone specified, use interpreter's timezone
                return ZonedDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(defaultTimezone));

            } catch (DateTimeParseException e) {
                return null;
            }
        };
    }
}

