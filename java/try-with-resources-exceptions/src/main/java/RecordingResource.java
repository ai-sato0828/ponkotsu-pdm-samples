import java.io.IOException;
import java.util.List;

final class RecordingResource implements AutoCloseable {
    private final String name;
    private final boolean failOnClose;
    private final List<String> trace;

    private RecordingResource(String name, boolean failOnClose, List<String> trace) {
        this.name = name;
        this.failOnClose = failOnClose;
        this.trace = trace;
    }

    static RecordingResource open(String name, boolean failOnOpen,
                                  boolean failOnClose, List<String> trace) throws IOException {
        trace.add("open:" + name);
        if (failOnOpen) {
            throw new IOException("open:" + name);
        }
        return new RecordingResource(name, failOnClose, trace);
    }

    @Override
    public void close() throws IOException {
        trace.add("close:" + name);
        if (failOnClose) {
            throw new IOException("close:" + name);
        }
    }
}
