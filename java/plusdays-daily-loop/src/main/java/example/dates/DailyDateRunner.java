package example.dates;

import java.time.LocalDate;
import java.util.function.Consumer;

public final class DailyDateRunner {
    private DailyDateRunner() {}

    public static void runInclusive(
            LocalDate from, LocalDate to, Consumer<LocalDate> dailyService) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("開始日は終了日以前にしてください");
        }

        LocalDate current = from;
        while (true) {
            dailyService.accept(current);
            if (current.equals(to)) {
                return;
            }
            current = current.plusDays(1);
        }
    }
}
