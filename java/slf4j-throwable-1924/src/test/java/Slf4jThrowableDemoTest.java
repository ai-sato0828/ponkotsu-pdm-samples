import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ResourceLock(Resources.SYSTEM_ERR)
class Slf4jThrowableDemoTest {
    @BeforeAll
    static void usesSlf4jSimple() {
        assertEquals("org.slf4j.impl.SimpleLoggerFactory",
                LoggerFactory.getILoggerFactory().getClass().getName());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "message-only, Save failed: Connection refused, 0",
        "throwable-only, 保存に失敗, 1",
        "id-then-throwable, userId=U001 の保存に失敗, 1",
        "extra-placeholder, userId=U001 の保存に失敗: {}, 1",
        "throwable-not-last, 保存に失敗: java.lang.IllegalStateException: Connection refused userId=U001, 0",
        "last-is-string, userId=U001 の保存に失敗: Connection refused, 0",
        "lone-throwable-placeholder, 保存に失敗: {}, 1",
    })
    void checksMessageAndStackTrace(String scenario, String expectedMessage, int traces) throws Exception {
        var output = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try (var capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setErr(capture);
            Slf4jThrowableDemo.run(scenario);
        } finally {
            System.setErr(previous);
        }
        String text = output.toString(StandardCharsets.UTF_8);
        Path folder = Files.createDirectories(Path.of("target", "slf4j-logs"));
        Files.writeString(folder.resolve(scenario + ".log"), text);

        var lines = text.lines().toList();
        var errorLines = lines.stream().filter(line -> line.contains(" ERROR Slf4jThrowableDemo - ")).toList();
        assertEquals(1, errorLines.size(), text);
        String error = errorLines.get(0);
        assertEquals(expectedMessage, error.substring(error.indexOf(" - ") + 3));
        assertEquals(traces, lines.stream().filter(line -> line.equals("java.lang.IllegalStateException: Connection refused")).count());
        assertEquals(traces, lines.stream().filter(line -> line.startsWith("\tat Slf4jThrowableDemo.run(")).count());
        assertEquals(traces, lines.stream().filter(line -> line.equals("Caused by: java.sql.SQLException: sample database unavailable")).count());
    }
}
