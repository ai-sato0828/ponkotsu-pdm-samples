package info.syachiku.finallyreturn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        System.out.println("quiz=" + FinallyExamples.getValue());
        List<String> events = new ArrayList<>();
        String value = FinallyExamples.getValueAfterCleanup(events);
        events.add("caller=" + value);
        System.out.println("events=" + events);

        RecordingOrderRepository repository = new RecordingOrderRepository(null);
        boolean result = new OrderService(repository, new RecordingAudit()).updateOrderBroken();
        System.out.println("order broken result=" + result + " saved=" + repository.savedCount());
        RecordingOrderRepository fixedRepository = new RecordingOrderRepository(null);
        boolean fixedResult = new OrderService(fixedRepository, new RecordingAudit()).updateOrder();
        System.out.println("order fixed result=" + fixedResult + " saved=" + fixedRepository.savedCount());
        RecordingAudit nameAudit = new RecordingAudit();
        NameService service = new NameService(() -> {
            throw new IllegalStateException("DB connection failed");
        }, nameAudit);
        System.out.println("name broken=" + service.loadNameBroken());
        try {
            service.loadName();
        } catch (RuntimeException failure) {
            System.out.println("name fixed=" + failure.getClass().getSimpleName()
                    + ": " + failure.getMessage() + " cleanupTotal=" + nameAudit.calls());
        }
        RecordingAudit brokenAudit = new RecordingAudit();
        int brokenNormal = CatchExamples.executeBroken(false, brokenAudit);
        int brokenFailure = CatchExamples.executeBroken(true, brokenAudit);
        System.out.println("catch broken normal=" + brokenNormal + " failed=" + brokenFailure
                + " catchCalls=" + brokenAudit.caughtCalls());
        RecordingAudit fixedAudit = new RecordingAudit();
        int fixedNormal = CatchExamples.execute(false, fixedAudit);
        int fixedFailure = CatchExamples.execute(true, fixedAudit);
        System.out.println("catch fixed normal=" + fixedNormal + " failed=" + fixedFailure
                + " catchCalls=" + fixedAudit.caughtCalls());

        ProbeReader reader = new ProbeReader("line1\nline2\n", null, null);
        List<String> lines = ResourceExamples.readLines(reader);
        System.out.println("resource normal=" + lines + " closeCalls=" + reader.closeCalls());
        ProbeReader failingReader = new ProbeReader("", new IOException("read failed"),
                new IOException("close failed"));
        try {
            ResourceExamples.readLines(failingReader);
        } catch (IOException failure) {
            System.out.println("resource both primary=" + failure.getMessage()
                    + " suppressed=" + suppressedMessages(failure)
                    + " closeCalls=" + failingReader.closeCalls());
        }
        try {
            ImportExamples.importWithoutSuppression(
                    () -> { throw new IllegalStateException("import failed"); },
                    () -> { throw new IllegalStateException("delete failed"); });
        } catch (RuntimeException failure) {
            System.out.println("import naive primary=" + failure.getMessage()
                    + " suppressed=" + suppressedMessages(failure));
        }
        try {
            ImportExamples.importPreservingFailure(
                    () -> { throw new IllegalStateException("import failed"); },
                    () -> { throw new IllegalStateException("delete failed"); });
        } catch (RuntimeException failure) {
            System.out.println("import preserved primary=" + failure.getMessage()
                    + " suppressed=" + suppressedMessages(failure));
        }
    }

    private static List<String> suppressedMessages(Throwable failure) {
        return Arrays.stream(failure.getSuppressed()).map(Throwable::getMessage).toList();
    }
}
