import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.List;

public class ReaderFailureDemo {
    static final class FailingReader extends Reader {
        boolean closed;

        @Override
        public int read(char[] buffer, int offset, int length) throws IOException {
            throw new IOException("simulated read failure");
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    // BEGIN readAll
    static List<String> readAll(BufferedReader reader) throws IOException {
        try (reader) {
            return reader.lines().toList();
        }
    }
    // END readAll

    public static void main(String[] args) throws IOException {
        FailingReader input = new FailingReader();
        try {
            readAll(new BufferedReader(input));
            throw new AssertionError("expected UncheckedIOException");
        } catch (UncheckedIOException expected) {
            if (!input.closed || !expected.getCause().getMessage().equals("simulated read failure")) {
                throw new AssertionError("expected IOException cause and close");
            }
            System.out.println("exception=UncheckedIOException, cause=IOException, closed=true");
        }
    }
}
