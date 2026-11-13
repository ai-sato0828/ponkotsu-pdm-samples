package info.syachiku.finallyreturn;

import java.util.Objects;

public final class CatchExamples {
    private CatchExamples() {
    }

    public static int executeBroken(boolean fail, RecordingAudit audit) {
        Objects.requireNonNull(audit, "audit");
        try {
            operation(fail);
            return 0;
        } catch (RuntimeException exception) {
            audit.recordCatch();
            return 1;
        } finally {
            audit.record();
            return 9;
        }
    }

    public static int execute(boolean fail, RecordingAudit audit) {
        Objects.requireNonNull(audit, "audit");
        try {
            operation(fail);
            return 0;
        } catch (RuntimeException exception) {
            audit.recordCatch();
            return 1;
        } finally {
            audit.record();
        }
    }

    private static void operation(boolean fail) {
        if (fail) {
            throw new IllegalStateException("operation failed");
        }
    }
}
