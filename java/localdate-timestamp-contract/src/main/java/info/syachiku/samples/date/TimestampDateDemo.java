package info.syachiku.samples.date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public final class TimestampDateDemo {
    private TimestampDateDemo() {
    }

    public static LocalDate parseDateOnly(String input) {
        return LocalDate.parse(input);
    }

    public static LocalDate parseLocalTimestampDate(String input) {
        return LocalDateTime.parse(input).toLocalDate();
    }

    public static LocalDate dateAtSourceOffset(String input) {
        return OffsetDateTime.parse(input).toLocalDate();
    }

    public static LocalDate dateInZone(String input, ZoneId zone) {
        return OffsetDateTime.parse(input).atZoneSameInstant(zone).toLocalDate();
    }

    public static void main(String[] args) {
        String localTimestamp = "2026-02-28T23:30:00";
        String offsetTimestamp = "2026-02-28T23:30:00-05:00";

        System.out.println("date only -> " + parseDateOnly("2026-02-28"));
        try {
            parseDateOnly(localTimestamp);
        } catch (DateTimeParseException error) {
            System.out.println(error.getMessage());
        }
        System.out.println("local timestamp date -> " + parseLocalTimestampDate(localTimestamp));

        String invalidTime = "2026-02-28T25:30:00";
        System.out.println("date prefix of invalid time -> " + parseDateOnly(invalidTime.substring(0, 10)));
        try {
            parseLocalTimestampDate(invalidTime);
        } catch (DateTimeParseException error) {
            System.out.println("invalid local timestamp -> " + error.getClass().getSimpleName());
        }

        System.out.println("source offset date -> " + dateAtSourceOffset(offsetTimestamp));
        System.out.println("Asia/Tokyo date -> " + dateInZone(offsetTimestamp, ZoneId.of("Asia/Tokyo")));
    }
}
