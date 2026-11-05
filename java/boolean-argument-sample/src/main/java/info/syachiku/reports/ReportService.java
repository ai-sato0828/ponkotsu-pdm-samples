package info.syachiku.reports;

import java.time.LocalDate;
import java.util.List;

public final class ReportService {
    private final ReportStore store;

    public ReportService(ReportStore store) {
        this.store = store;
    }

    public Report preview(LocalDate reportDate, List<Integer> amountsYen) {
        return buildReport(reportDate, amountsYen);
    }

    public Report publish(LocalDate reportDate, List<Integer> amountsYen) {
        Report report = buildReport(reportDate, amountsYen);
        store.save(report);
        return report;
    }

    private static Report buildReport(LocalDate reportDate, List<Integer> amountsYen) {
        long totalYen = amountsYen.stream().mapToLong(Integer::longValue).sum();
        return new Report(reportDate, totalYen);
    }
}
