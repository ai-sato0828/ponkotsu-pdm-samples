package info.syachiku.finallyreturn;

import java.util.Objects;

public final class ImportExamples {
    private ImportExamples() {
    }

    public static void importWithoutSuppression(Runnable importOrders, Runnable deleteTemporaryFile) {
        Objects.requireNonNull(importOrders, "importOrders");
        Objects.requireNonNull(deleteTemporaryFile, "deleteTemporaryFile");
        try {
            importOrders.run();
        } finally {
            deleteTemporaryFile.run();
        }
    }

    // RuntimeException同士の優先方針を示す例。checked例外/Error全般の共通部品ではない。
    public static void importPreservingFailure(Runnable importOrders, Runnable deleteTemporaryFile) {
        Objects.requireNonNull(importOrders, "importOrders");
        Objects.requireNonNull(deleteTemporaryFile, "deleteTemporaryFile");
        RuntimeException failure = null;
        try {
            importOrders.run();
        } catch (RuntimeException exception) {
            failure = exception;
            throw exception;
        } finally {
            try {
                deleteTemporaryFile.run();
            } catch (RuntimeException cleanupError) {
                if (failure != null) {
                    if (failure != cleanupError) {
                        failure.addSuppressed(cleanupError);
                    }
                } else {
                    throw cleanupError;
                }
            }
        }
    }
}
