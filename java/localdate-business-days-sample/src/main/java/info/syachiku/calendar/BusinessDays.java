package info.syachiku.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

public final class BusinessDays {
    private static final int MAX_BUSINESS_DAYS = 60;

    private BusinessDays() {
    }

    public static LocalDate add(LocalDate receivedOn, int businessDays, Set<LocalDate> closedDates) {
        Objects.requireNonNull(receivedOn, "receivedOn");
        if (businessDays < 0 || businessDays > MAX_BUSINESS_DAYS) {
            throw new IllegalArgumentException("営業日数は0〜60で指定してください");
        }
        Set<LocalDate> closures = Set.copyOf(closedDates);

        LocalDate date = receivedOn;
        int remaining = businessDays;
        while (remaining > 0) {
            date = date.plusDays(1);
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY
                    && dayOfWeek != DayOfWeek.SUNDAY
                    && !closures.contains(date)) {
                remaining--;
            }
        }
        return date;
    }
}
