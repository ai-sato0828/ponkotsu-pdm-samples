package info.syachiku.reports;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportServiceTest {
    private static final LocalDate REPORT_DATE = LocalDate.of(2026, 11, 5);
    private static final List<Integer> AMOUNTS_YEN = List.of(1_200, 800);
    private static final Report EXPECTED = new Report(REPORT_DATE, 2_000);

    private final List<Report> saved = new ArrayList<>();
    private final ReportService service = new ReportService(saved::add);

    @Test
    void reproducesTheLegacyPreviewSavingAReport() {
        var screen = new LegacyReportScreen(new LegacyReportService(saved::add));

        assertEquals(EXPECTED, screen.showPreview(REPORT_DATE, AMOUNTS_YEN));
        assertEquals(List.of(EXPECTED), saved);
    }

    @Test
    void legacyGenerateWithFalseDoesNotSave() {
        var legacy = new LegacyReportService(saved::add);

        assertEquals(EXPECTED, legacy.generate(REPORT_DATE, AMOUNTS_YEN, false));
        assertTrue(saved.isEmpty());
    }

    @Test
    void previewReturnsReportWithoutSaving() {
        assertEquals(EXPECTED, service.preview(REPORT_DATE, AMOUNTS_YEN));
        assertTrue(saved.isEmpty());
    }

    @Test
    void publishSavesTheReturnedReportExactlyOnce() {
        Report published = service.publish(REPORT_DATE, AMOUNTS_YEN);

        assertEquals(EXPECTED, published);
        assertEquals(List.of(published), saved);
    }

    @Test
    void sameInputProducesTheSameReportOnBothPaths() {
        Report previewed = service.preview(REPORT_DATE, AMOUNTS_YEN);
        Report published = service.publish(REPORT_DATE, AMOUNTS_YEN);

        assertEquals(previewed, published);
        assertEquals(List.of(published), saved);
    }

    @Test
    void repeatingPreviewDoesNotSave() {
        service.preview(REPORT_DATE, AMOUNTS_YEN);
        service.preview(REPORT_DATE, AMOUNTS_YEN);

        assertTrue(saved.isEmpty());
    }
}
