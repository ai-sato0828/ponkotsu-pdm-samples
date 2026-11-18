package example;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public final class OffsetTimestampDateDemo {
    static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    private OffsetTimestampDateDemo() {}

    public static void main(String[] args) {
        String utcInput = "2026-02-28T23:30:00Z";
        String plusNineInput = "2026-03-01T08:30:00+09:00";

        try {
            LocalDateTime.parse(utcInput);
            throw new IllegalStateException("Expected the local parser to reject the UTC offset");
        } catch (DateTimeParseException e) {
            System.out.printf("LocalDateTime.parse: %s errorIndex=%d%n",
                    e.getClass().getSimpleName(), e.getErrorIndex());
        }

        OffsetDateTime utc = OffsetDateTime.parse(utcInput);
        OffsetDateTime plusNine = OffsetDateTime.parse(plusNineInput);
        printDates(utcInput, utc);
        printDates(plusNineInput, plusNine);
        System.out.println("sameInstant=" + utc.toInstant().equals(plusNine.toInstant()));
    }

    static LocalDate dateInZone(OffsetDateTime timestamp, ZoneId zone) {
        return timestamp.atZoneSameInstant(zone).toLocalDate();
    }

    private static void printDates(String input, OffsetDateTime timestamp) {
        System.out.printf("%s writtenDate=%s tokyoDate=%s%n",
                input, timestamp.toLocalDate(), dateInZone(timestamp, TOKYO));
    }
}
