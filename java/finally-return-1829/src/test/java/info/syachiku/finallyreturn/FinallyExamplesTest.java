package info.syachiku.finallyreturn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

class FinallyExamplesTest {
    @Test
    void F01_finallyReturn_overwritesTryValue() {
        assertEquals("B", FinallyExamples.getValue());
    }

    @Test
    void F02_cleanup_runsBeforeCallerReceivesA() {
        List<String> events = new ArrayList<>();
        String value = FinallyExamples.getValueAfterCleanup(events);
        events.add("caller=" + value);
        assertEquals("A", value);
        assertEquals(List.of("cleanup", "caller=A"), events);
    }

    @Test
    void F03_brokenOrder_savesButReturnsFalse() {
        RecordingOrderRepository repository = new RecordingOrderRepository(null);
        RecordingAudit audit = new RecordingAudit();
        assertFalse(new OrderService(repository, audit).updateOrderBroken());
        assertEquals(1, repository.saveCalls());
        assertEquals(1, repository.savedCount());
        assertEquals(1, audit.calls());
    }

    @Test
    void F04_fixedOrder_savesAndReturnsTrue() {
        RecordingOrderRepository repository = new RecordingOrderRepository(null);
        RecordingAudit audit = new RecordingAudit();
        assertTrue(new OrderService(repository, audit).updateOrder());
        assertEquals(1, repository.savedCount());
        assertEquals(1, audit.calls());
    }

    @Test
    void F05_brokenOrder_swallowsSaveFailure() {
        RecordingOrderRepository repository = new RecordingOrderRepository(
                new IllegalStateException("save failed"));
        RecordingAudit audit = new RecordingAudit();
        assertFalse(new OrderService(repository, audit).updateOrderBroken());
        assertEquals(1, repository.saveCalls());
        assertEquals(0, repository.savedCount());
        assertEquals(1, audit.calls());
    }

    @Test
    void F06_fixedOrder_propagatesSameSaveFailure() {
        IllegalStateException failure = new IllegalStateException("save failed");
        RecordingOrderRepository repository = new RecordingOrderRepository(failure);
        RecordingAudit audit = new RecordingAudit();
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> new OrderService(repository, audit).updateOrder()));
        assertEquals(0, repository.savedCount());
        assertEquals(1, audit.calls());
    }

    @Test
    void F07_brokenName_returnsGuestDespiteFailure() {
        RecordingAudit audit = new RecordingAudit();
        NameService service = new NameService(() -> {
            throw new IllegalStateException("DB connection failed");
        }, audit);
        assertEquals("guest", service.loadNameBroken());
        assertEquals(1, audit.calls());
    }

    @Test
    void F08_fixedName_propagatesFailureToCaller() {
        IllegalStateException failure = new IllegalStateException("DB connection failed");
        RecordingAudit audit = new RecordingAudit();
        NameService service = new NameService(() -> { throw failure; }, audit);
        IllegalStateException actual = assertThrows(IllegalStateException.class, service::loadName);
        assertSame(failure, actual);
        assertEquals("DB connection failed", actual.getMessage());
        assertEquals(1, audit.calls());
    }

    @Test
    void F09_fixedName_returnsActualName() {
        RecordingAudit audit = new RecordingAudit();
        assertEquals("Alice", new NameService(() -> "Alice", audit).loadName());
        assertEquals(1, audit.calls());
    }

    @Test
    void F10_fixedName_rejectsRepositoryNullInsteadOfFallback() {
        RecordingAudit audit = new RecordingAudit();
        NameService service = new NameService(() -> null, audit);
        NullPointerException actual = assertThrows(NullPointerException.class, service::loadName);
        assertEquals("repository returned null", actual.getMessage());
        assertEquals(1, audit.calls());
    }

    @Test
    void F11_brokenCatch_normalPathReturnsNine() {
        RecordingAudit audit = new RecordingAudit();
        assertEquals(9, CatchExamples.executeBroken(false, audit));
        assertEquals(0, audit.caughtCalls());
        assertEquals(1, audit.calls());
    }

    @Test
    void F12_brokenCatch_realFailureEntersCatchButReturnsNine() {
        RecordingAudit audit = new RecordingAudit();
        assertEquals(9, CatchExamples.executeBroken(true, audit));
        assertEquals(1, audit.caughtCalls());
        assertEquals(1, audit.calls());
    }

    @Test
    void F13_fixedCatch_normalPathReturnsZero() {
        RecordingAudit audit = new RecordingAudit();
        assertEquals(0, CatchExamples.execute(false, audit));
        assertEquals(0, audit.caughtCalls());
        assertEquals(1, audit.calls());
    }

    @Test
    void F14_fixedCatch_failureReturnsOneAfterActualCatch() {
        RecordingAudit audit = new RecordingAudit();
        assertEquals(1, CatchExamples.execute(true, audit));
        assertEquals(1, audit.caughtCalls());
        assertEquals(1, audit.calls());
    }

    @Test
    void F15_lock_isReleasedAfterSuccess() {
        ReentrantLock lock = new ReentrantLock();
        AtomicInteger updates = new AtomicInteger();
        LockExamples.update(lock, updates::incrementAndGet);
        assertEquals(1, updates.get());
        assertFalse(lock.isLocked());
    }

    @Test
    void F16_lock_isReleasedAndSameFailurePropagates() {
        ReentrantLock lock = new ReentrantLock();
        IllegalStateException failure = new IllegalStateException("update failed");
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> LockExamples.update(lock, () -> { throw failure; })));
        assertFalse(lock.isLocked());
    }

    @Test
    void F17_resource_returnsLinesAndCallsCloseOnce() throws IOException {
        ProbeReader reader = new ProbeReader("line1\nline2\n", null, null);
        assertEquals(List.of("line1", "line2"), ResourceExamples.readLines(reader));
        assertEquals(1, reader.closeCalls());
    }

    @Test
    void F18_resource_readFailurePropagatesAndCloseRuns() {
        IOException failure = new IOException("read failed");
        ProbeReader reader = new ProbeReader("", failure, null);
        assertSame(failure, assertThrows(IOException.class, () -> ResourceExamples.readLines(reader)));
        assertEquals(1, reader.closeCalls());
    }

    @Test
    void F19_resource_closeFailureReplacesNormalReturn() {
        IOException close = new IOException("close failed");
        ProbeReader reader = new ProbeReader("line1", null, close);
        assertSame(close, assertThrows(IOException.class, () -> ResourceExamples.readLines(reader)));
        assertEquals(0, close.getSuppressed().length);
        assertEquals(1, reader.closeCalls());
    }

    @Test
    void F20_resource_bothFailuresKeepReadPrimaryAndSuppressClose() {
        IOException read = new IOException("read failed");
        IOException close = new IOException("close failed");
        ProbeReader reader = new ProbeReader("", read, close);
        IOException actual = assertThrows(IOException.class, () -> ResourceExamples.readLines(reader));
        assertSame(read, actual);
        assertArrayEquals(new Throwable[]{close}, actual.getSuppressed());
        assertEquals(1, reader.closeCalls());
    }

    @Test
    void F21_resource_emptyInputReturnsEmptyListNotNull() throws IOException {
        ProbeReader reader = new ProbeReader("", null, null);
        assertEquals(List.of(), ResourceExamples.readLines(reader));
        assertEquals(1, reader.closeCalls());
    }

    @Test
    void F22_path_readsRealUtf8File(@TempDir Path directory) throws IOException {
        Path path = directory.resolve("orders.txt");
        Files.writeString(path, "一行目\n二行目\n", StandardCharsets.UTF_8);
        assertEquals(List.of("一行目", "二行目"), ResourceExamples.readLines(path));
    }

    @Test
    void F23_path_openFailureRemainsCheckedIOException(@TempDir Path directory) {
        assertThrows(NoSuchFileException.class,
                () -> ResourceExamples.readLines(directory.resolve("missing.txt")));
    }

    @Test
    void F24_naiveCleanup_hidesImportFailure() {
        IllegalStateException read = new IllegalStateException("import failed");
        IllegalStateException cleanup = new IllegalStateException("delete failed");
        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> ImportExamples.importWithoutSuppression(
                        () -> { throw read; }, () -> { throw cleanup; }));
        assertSame(cleanup, actual);
        assertEquals(0, actual.getSuppressed().length);
    }

    @Test
    void F25_preservingCleanup_keepsImportAndSuppressesDelete() {
        IllegalStateException read = new IllegalStateException("import failed");
        IllegalStateException cleanup = new IllegalStateException("delete failed");
        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> ImportExamples.importPreservingFailure(
                        () -> { throw read; }, () -> { throw cleanup; }));
        assertSame(read, actual);
        assertArrayEquals(new Throwable[]{cleanup}, actual.getSuppressed());
    }

    @Test
    void F26_preservingCleanup_onlyDeleteFailurePropagates() {
        IllegalStateException cleanup = new IllegalStateException("delete failed");
        assertSame(cleanup, assertThrows(IllegalStateException.class,
                () -> ImportExamples.importPreservingFailure(() -> {}, () -> { throw cleanup; })));
        assertEquals(0, cleanup.getSuppressed().length);
    }

    @Test
    void F27_preservingCleanup_onlyImportFailurePropagatesAndDeleteRuns() {
        IllegalStateException read = new IllegalStateException("import failed");
        AtomicInteger deletes = new AtomicInteger();
        assertSame(read, assertThrows(IllegalStateException.class,
                () -> ImportExamples.importPreservingFailure(
                        () -> { throw read; }, deletes::incrementAndGet)));
        assertEquals(1, deletes.get());
        assertEquals(0, read.getSuppressed().length);
    }

    @Test
    void F28_preservingCleanup_bothActionsRunOnSuccess() {
        List<String> actions = new ArrayList<>();
        ImportExamples.importPreservingFailure(() -> actions.add("import"), () -> actions.add("delete"));
        assertEquals(List.of("import", "delete"), actions);
    }

    @Test
    void F29_preservingCleanup_avoidsSelfSuppressionForSameException() {
        IllegalStateException failure = new IllegalStateException("shared test instance");
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> ImportExamples.importPreservingFailure(
                        () -> { throw failure; }, () -> { throw failure; })));
        assertEquals(0, failure.getSuppressed().length);
    }

    @Test
    void F30_resource_rejectsNullInputs() {
        assertThrows(NullPointerException.class, () -> ResourceExamples.readLines((Path) null));
        assertThrows(NullPointerException.class, () -> ResourceExamples.readLines((BufferedReader) null));
    }
}
