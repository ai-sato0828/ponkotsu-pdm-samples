package example.booking;

import java.time.Duration;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeSlotTest {
    @Test
    void preservesTheDuration() {
        LocalTime start = LocalTime.parse("09:00");
        LocalTime end = LocalTime.parse("10:00");

        assertEquals(Duration.ofHours(1), new TimeSlot(start, end).duration());
        assertEquals(Duration.ofHours(1), ReservationChecks.duration(start, end));
    }

    @ParameterizedTest
    @CsvSource({
        "10:00, 09:00",
        "09:00, 09:00",
        ", 10:00",
        "09:00, "
    })
    void rejectsInvalidPairsAtConstruction(String startText, String endText) {
        LocalTime start = parseNullable(startText);
        LocalTime end = parseNullable(endText);

        assertThrows(IllegalArgumentException.class, () -> new TimeSlot(start, end));
        assertThrows(IllegalArgumentException.class, () -> ReservationChecks.duration(start, end));
    }

    @ParameterizedTest
    @CsvSource({
        "08:30, 09:00, false",
        "09:30, 10:30, true",
        "10:00, 11:00, false",
        "09:00, 10:00, true"
    })
    void preservesTheOverlapRule(String startText, String endText, boolean expected) {
        LocalTime start = LocalTime.parse("09:00");
        LocalTime end = LocalTime.parse("10:00");
        LocalTime otherStart = LocalTime.parse(startText);
        LocalTime otherEnd = LocalTime.parse(endText);

        assertEquals(expected, new TimeSlot(start, end).overlaps(new TimeSlot(otherStart, otherEnd)));
        assertEquals(expected, ReservationChecks.overlaps(start, end, otherStart, otherEnd));
    }

    private LocalTime parseNullable(String value) {
        return value == null ? null : LocalTime.parse(value);
    }
}
