package info.syachiku.snapshot;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface SnapshotWriter {
    void write(OutputStream output) throws IOException;
}
