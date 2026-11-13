package info.syachiku.finallyreturn;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public final class LockExamples {
    private LockExamples() {
    }

    public static void update(ReentrantLock lock, Runnable updateSharedState) {
        Objects.requireNonNull(lock, "lock");
        Objects.requireNonNull(updateSharedState, "updateSharedState");
        lock.lock();
        try {
            updateSharedState.run();
        } finally {
            lock.unlock();
        }
    }
}
