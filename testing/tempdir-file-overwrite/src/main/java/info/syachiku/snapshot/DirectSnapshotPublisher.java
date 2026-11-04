package info.syachiku.snapshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DirectSnapshotPublisher {
    public void publish(Path target, SnapshotWriter writer) throws IOException {
        try (var output = Files.newOutputStream(target)) {
            writer.write(output);
        }
    }
}
