package example.inspection;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public final class InspectionScheduleDemo {
    private InspectionScheduleDemo() {
    }

    static List<LocalDate> rollingDates(LocalDate firstDate, int monthCount) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate previous = firstDate;

        for (int i = 0; i < monthCount; i++) {
            dates.add(previous);
            previous = previous.plusMonths(1);
        }
        return dates;
    }

    public static void main(String[] args) {
        LocalDate january31 = LocalDate.of(2026, 1, 31);
        YearMonth january = YearMonth.of(2026, 1);
        YearMonth march = YearMonth.of(2026, 3);
        var day31 = new MonthlyInspectionSchedule(31);

        System.out.println("before=" + rollingDates(january31, 3));
        System.out.println("after=" + day31.datesFrom(january, 3));
        System.out.println("day30=" + new MonthlyInspectionSchedule(30).dateIn(march));
        System.out.println("monthEnd=" + march.atEndOfMonth());
    }
}
