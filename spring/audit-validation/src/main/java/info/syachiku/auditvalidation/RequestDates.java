package info.syachiku.auditvalidation;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@ValidDateRange
record RequestDates(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate) {
}
