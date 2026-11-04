package info.syachiku.auditvalidation;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@ValidTaskWindow
record TaskInput(
        @NotBlank(message = "件名を入力してください") String title,
        LocalDate startDate,
        LocalDate endDate) {
}
