package info.syachiku.reports;

import java.time.LocalDate;
import java.util.List;

final class LegacyReportScreen {
    private final LegacyReportService service;

    LegacyReportScreen(LegacyReportService service) {
        this.service = service;
    }

    Report showPreview(LocalDate reportDate, List<Integer> amountsYen) {
        return service.generate(reportDate, amountsYen, true);
    }
}

final class LegacyReportService {
    private final ReportStore store;

    LegacyReportService(ReportStore store) {
        this.store = store;
    }

    Report generate(LocalDate reportDate, List<Integer> amountsYen, boolean save) {
        long totalYen = amountsYen.stream().mapToLong(Integer::longValue).sum();
        Report report = new Report(reportDate, totalYen);
        if (save) {
            store.save(report);
        }
        return report;
    }
}
