package info.syachiku.snapshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public final class SnapshotPublisher {
    private final AtomicMove move;

    public SnapshotPublisher() {
        this((staged, target) -> Files.move(staged, target, ATOMIC_MOVE));
    }

    SnapshotPublisher(AtomicMove move) {
        this.move = move;
    }

    public void publish(Path target, SnapshotWriter writer) throws IOException {
        Path destination = target.toAbsolutePath();
        Path staged = Files.createTempFile(destination.getParent(), ".report-", ".tmp");
        try {
            try (var output = Files.newOutputStream(staged)) {
                writer.write(output);
            }
            move.move(staged, destination);
        } catch (IOException | RuntimeException failure) {
            try {
                Files.deleteIfExists(staged);
            } catch (IOException | SecurityException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    @FunctionalInterface
    interface AtomicMove {
        void move(Path staged, Path target) throws IOException;
    }
}
