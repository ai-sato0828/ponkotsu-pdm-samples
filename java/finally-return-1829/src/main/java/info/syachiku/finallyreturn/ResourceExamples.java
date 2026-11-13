package info.syachiku.finallyreturn;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ResourceExamples {
    private ResourceExamples() {
    }

    public static List<String> readLines(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        return readLines(Files.newBufferedReader(path, StandardCharsets.UTF_8));
    }

    // 受け取ったreaderをこのメソッドが所有し、終了時にcloseする契約。
    public static List<String> readLines(BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader, "reader");
        try (reader) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return List.copyOf(lines);
        }
    }
}
