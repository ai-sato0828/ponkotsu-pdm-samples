package example.inspection;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record MonthlyInspectionSchedule(int dayOfMonth) {
    public MonthlyInspectionSchedule {
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new IllegalArgumentException("dayOfMonth must be 1 to 31");
        }
    }

    public LocalDate dateIn(YearMonth month) {
        int day = Math.min(dayOfMonth, month.lengthOfMonth());
        return month.atDay(day);
    }

    public List<LocalDate> datesFrom(YearMonth firstMonth, int monthCount) {
        Objects.requireNonNull(firstMonth, "firstMonth");
        if (monthCount < 0) {
            throw new IllegalArgumentException("monthCount must not be negative");
        }

        List<LocalDate> dates = new ArrayList<>();
        for (int i = 0; i < monthCount; i++) {
            dates.add(dateIn(firstMonth.plusMonths(i)));
        }
        return List.copyOf(dates);
    }
}
