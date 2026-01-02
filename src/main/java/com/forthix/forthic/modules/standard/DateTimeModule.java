package com.forthix.forthic.modules.standard;

import com.forthix.forthic.annotations.Word;
import com.forthix.forthic.interpreter.BareInterpreter;
import com.forthix.forthic.module.DecoratedModule;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date and time operations using java.time API for timezone-aware datetime manipulation.
 *
 * Categories:
 * - Current: TODAY, NOW
 * - Time adjustment: AM, PM
 * - Conversion to: >TIME, >DATE, >DATETIME, AT
 * - Conversion from: TIME>STR, DATE>STR, DATE>INT
 * - Timestamps: >TIMESTAMP, TIMESTAMP>DATETIME
 * - Date math: ADD-DAYS, SUBTRACT-DATES
 *
 * Type Mapping (TypeScript Temporal → Java):
 * - Temporal.PlainDate → LocalDate
 * - Temporal.PlainTime → LocalTime
 * - Temporal.PlainDateTime → LocalDateTime
 * - Temporal.ZonedDateTime → ZonedDateTime
 * - Temporal.Instant → Instant
 */
public class DateTimeModule extends DecoratedModule {

    public DateTimeModule() {
        super("datetime");
    }

    // ===== Current Date/Time =====

    @Word(stackEffect = "( -- date:LocalDate )", description = "Get current date", isDirect = true)
    public void TODAY(BareInterpreter interp) {
        ZoneId zone = ((com.forthix.forthic.interpreter.StandardInterpreter) interp).getZoneId();
        LocalDate today = LocalDate.now(zone);
        interp.stackPush(today);
    }

    @Word(stackEffect = "( -- datetime:LocalDateTime )", description = "Get current datetime", isDirect = true)
    public void NOW(BareInterpreter interp) {
        ZoneId zone = ((com.forthix.forthic.interpreter.StandardInterpreter) interp).getZoneId();
        LocalDateTime now = LocalDateTime.now(zone);
        interp.stackPush(now);
    }

    // ===== Time Adjustment =====

    @Word(stackEffect = "( time:LocalTime -- time:LocalTime )", description = "Convert time to AM (subtract 12 from hour if >= 12)")
    public Object AM(Object time) {
        if (time == null || !(time instanceof LocalTime)) {
            return time;
        }

        LocalTime localTime = (LocalTime) time;
        if (localTime.getHour() >= 12) {
            return localTime.minusHours(12);
        }
        return time;
    }

    @Word(stackEffect = "( time:LocalTime -- time:LocalTime )", description = "Convert time to PM (add 12 to hour if < 12)")
    public Object PM(Object time) {
        if (time == null || !(time instanceof LocalTime)) {
            return time;
        }

        LocalTime localTime = (LocalTime) time;
        if (localTime.getHour() < 12) {
            return localTime.plusHours(12);
        }
        return time;
    }

    // ===== Conversion TO datetime types =====

    @Word(stackEffect = "( item:any -- time:LocalTime )", description = "Convert string or datetime to LocalTime", name = ">TIME")
    public Object to_TIME(Object item) {
        if (item == null) {
            return null;
        }

        // If already a LocalTime, return it
        if (item instanceof LocalTime) {
            return item;
        }

        // If it's a LocalDateTime or ZonedDateTime, extract the time
        if (item instanceof LocalDateTime) {
            return ((LocalDateTime) item).toLocalTime();
        }
        if (item instanceof ZonedDateTime) {
            return ((ZonedDateTime) item).toLocalTime();
        }

        // Otherwise, parse as string
        String str = item.toString().trim();

        // Handle "HH:MM AM/PM" format
        Pattern ampmPattern = Pattern.compile("^(\\d{1,2}):(\\d{2})\\s*(AM|PM)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = ampmPattern.matcher(str);
        if (matcher.matches()) {
            int hour = Integer.parseInt(matcher.group(1));
            int minute = Integer.parseInt(matcher.group(2));
            String meridiem = matcher.group(3).toUpperCase();

            if ("PM".equals(meridiem) && hour < 12) {
                hour += 12;
            } else if ("AM".equals(meridiem) && hour == 12) {
                hour = 0;
            }

            return LocalTime.of(hour, minute);
        }

        // Try standard time parsing (HH:MM or HH:MM:SS)
        try {
            return LocalTime.parse(str);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Word(stackEffect = "( item:any -- date:LocalDate )", description = "Convert string or datetime to LocalDate", name = ">DATE")
    public Object to_DATE(Object item) {
        if (item == null) {
            return null;
        }

        // If already a LocalDate, return it
        if (item instanceof LocalDate) {
            return item;
        }

        // If it's a LocalDateTime or ZonedDateTime, extract the date
        if (item instanceof LocalDateTime) {
            return ((LocalDateTime) item).toLocalDate();
        }
        if (item instanceof ZonedDateTime) {
            return ((ZonedDateTime) item).toLocalDate();
        }

        // Otherwise, parse as string
        String str = item.toString().trim();

        // Try standard ISO format (YYYY-MM-DD)
        try {
            return LocalDate.parse(str);
        } catch (DateTimeParseException e) {
            // Try parsing with various formats
            String[] formats = {
                "yyyy/MM/dd",
                "MM/dd/yyyy",
                "dd/MM/yyyy",
                "M/d/yyyy"
            };

            for (String format : formats) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    return LocalDate.parse(str, formatter);
                } catch (DateTimeParseException ignored) {
                }
            }
        }

        return null;
    }

    @Word(stackEffect = "( str_or_timestamp:any -- datetime:ZonedDateTime )", description = "Convert string or timestamp to ZonedDateTime", isDirect = true, name = ">DATETIME")
    public void to_DATETIME(BareInterpreter interp) {
        Object item = interp.stackPop();

        if (item == null) {
            interp.stackPush(null);
            return;
        }

        ZoneId zone = ((com.forthix.forthic.interpreter.StandardInterpreter) interp).getZoneId();

        // If already a ZonedDateTime, return it
        if (item instanceof ZonedDateTime) {
            interp.stackPush(item);
            return;
        }

        // If it's a number, treat as Unix timestamp (seconds)
        if (item instanceof Number) {
            long timestamp = ((Number) item).longValue();
            Instant instant = Instant.ofEpochSecond(timestamp);
            ZonedDateTime zoned = instant.atZone(zone);
            interp.stackPush(zoned);
            return;
        }

        // If it's a LocalDateTime, convert to ZonedDateTime
        if (item instanceof LocalDateTime) {
            ZonedDateTime zoned = ((LocalDateTime) item).atZone(zone);
            interp.stackPush(zoned);
            return;
        }

        // Otherwise, parse as string
        String str = item.toString().trim();

        try {
            // Try parsing as ISO datetime string
            LocalDateTime localDateTime = LocalDateTime.parse(str);
            ZonedDateTime zoned = localDateTime.atZone(zone);
            interp.stackPush(zoned);
        } catch (DateTimeParseException e) {
            interp.stackPush(null);
        }
    }

    @Word(stackEffect = "( date:LocalDate time:LocalTime -- datetime:ZonedDateTime )", description = "Combine date and time into datetime", isDirect = true)
    public void AT(BareInterpreter interp) {
        Object time = interp.stackPop();
        Object date = interp.stackPop();

        if (date == null || time == null) {
            interp.stackPush(null);
            return;
        }

        if (!(date instanceof LocalDate) || !(time instanceof LocalTime)) {
            interp.stackPush(null);
            return;
        }

        LocalDate localDate = (LocalDate) date;
        LocalTime localTime = (LocalTime) time;

        // Combine into LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

        // Convert to ZonedDateTime using interpreter's timezone
        ZoneId zone = ((com.forthix.forthic.interpreter.StandardInterpreter) interp).getZoneId();
        ZonedDateTime zoned = localDateTime.atZone(zone);
        interp.stackPush(zoned);
    }

    // ===== Conversion FROM datetime types =====

    @Word(stackEffect = "( time:LocalTime -- str:string )", description = "Convert time to HH:MM string", name = "TIME>STR")
    public String TIME_to_STR(Object time) {
        if (time == null || !(time instanceof LocalTime)) {
            return "";
        }

        LocalTime localTime = (LocalTime) time;
        return String.format("%02d:%02d", localTime.getHour(), localTime.getMinute());
    }

    @Word(stackEffect = "( date:LocalDate -- str:string )", description = "Convert date to YYYY-MM-DD string", name = "DATE>STR")
    public String DATE_to_STR(Object date) {
        if (date == null || !(date instanceof LocalDate)) {
            return "";
        }

        LocalDate localDate = (LocalDate) date;
        return localDate.toString();
    }

    @Word(stackEffect = "( date:LocalDate -- int:number )", description = "Convert date to integer (YYYYMMDD)", name = "DATE>INT")
    public Object DATE_to_INT(Object date) {
        if (date == null || !(date instanceof LocalDate)) {
            return null;
        }

        LocalDate localDate = (LocalDate) date;
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();

        return year * 10000 + month * 100 + day;
    }

    // ===== Timestamps =====

    @Word(stackEffect = "( datetime:ZonedDateTime -- timestamp:number )", description = "Convert datetime to Unix timestamp (seconds)", isDirect = true, name = ">TIMESTAMP")
    public void to_TIMESTAMP(BareInterpreter interp) {
        Object datetime = interp.stackPop();

        if (datetime == null) {
            interp.stackPush(null);
            return;
        }

        Instant instant;

        // Convert to Instant
        if (datetime instanceof ZonedDateTime) {
            instant = ((ZonedDateTime) datetime).toInstant();
        } else if (datetime instanceof LocalDateTime) {
            // Convert LocalDateTime to ZonedDateTime first using interpreter timezone
            ZoneId zone = ((com.forthix.forthic.interpreter.StandardInterpreter) interp).getZoneId();
            ZonedDateTime zoned = ((LocalDateTime) datetime).atZone(zone);
            instant = zoned.toInstant();
        } else if (datetime instanceof Instant) {
            instant = (Instant) datetime;
        } else {
            interp.stackPush(null);
            return;
        }

        // Convert to seconds
        long timestamp = instant.getEpochSecond();
        interp.stackPush(timestamp);
    }

    @Word(stackEffect = "( timestamp:number -- datetime:ZonedDateTime )", description = "Convert Unix timestamp (seconds) to datetime", isDirect = true, name = "TIMESTAMP>DATETIME")
    public void TIMESTAMP_to_DATETIME(BareInterpreter interp) {
        Object timestamp = interp.stackPop();

        if (timestamp == null || !(timestamp instanceof Number)) {
            interp.stackPush(null);
            return;
        }

        long timestampSeconds = ((Number) timestamp).longValue();
        Instant instant = Instant.ofEpochSecond(timestampSeconds);
        ZoneId zone = ((com.forthix.forthic.interpreter.StandardInterpreter) interp).getZoneId();
        ZonedDateTime zoned = instant.atZone(zone);
        interp.stackPush(zoned);
    }

    // ===== Date Math =====

    @Word(stackEffect = "( date:LocalDate num_days:number -- date:LocalDate )", description = "Add days to a date", name = "ADD-DAYS")
    public Object ADD_DAYS(Object date, Object num_days) {
        if (date == null || !(date instanceof LocalDate) || num_days == null) {
            return null;
        }

        LocalDate localDate = (LocalDate) date;
        long days = ((Number) num_days).longValue();
        return localDate.plusDays(days);
    }

    @Word(stackEffect = "( date1:LocalDate date2:LocalDate -- num_days:number )", description = "Get difference in days between dates (date1 - date2)", name = "SUBTRACT-DATES")
    public Object SUBTRACT_DATES(Object date1, Object date2) {
        if (date1 == null || !(date1 instanceof LocalDate) || date2 == null || !(date2 instanceof LocalDate)) {
            return null;
        }

        LocalDate localDate1 = (LocalDate) date1;
        LocalDate localDate2 = (LocalDate) date2;

        // Calculate days between date2 and date1 (date1 - date2)
        long days = ChronoUnit.DAYS.between(localDate2, localDate1);
        return days;
    }
}
