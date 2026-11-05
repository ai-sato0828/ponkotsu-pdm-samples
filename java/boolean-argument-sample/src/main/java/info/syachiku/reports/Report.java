package info.syachiku.reports;

import java.time.LocalDate;

public record Report(LocalDate reportDate, long totalYen) {
}
