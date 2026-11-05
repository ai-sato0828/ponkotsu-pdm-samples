package info.syachiku.reports;

@FunctionalInterface
public interface ReportStore {
    void save(Report report);
}
