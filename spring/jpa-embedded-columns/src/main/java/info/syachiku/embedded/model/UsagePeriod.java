package info.syachiku.embedded.model;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class UsagePeriod {
    private LocalDate startDate;
    private LocalDate endDate;

    protected UsagePeriod() {}

    public UsagePeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = Objects.requireNonNull(endDate);
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("終了日は開始日以降にしてください");
        }
    }

    public boolean includes(LocalDate target) {
        return !target.isBefore(startDate) && !target.isAfter(endDate);
    }
}
