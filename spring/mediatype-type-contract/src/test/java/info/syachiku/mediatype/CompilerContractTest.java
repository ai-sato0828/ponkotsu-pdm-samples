package info.syachiku.mediatype;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class CompilerContractTest {
    record Case(String name, String before, String after, String errorCode) {}
    @TempDir Path classes;

    static Stream<Case> cases() {
        return Stream.of(
                new Case("AnnotationObject", "MediaType.APPLICATION_JSON)",
                        "MediaType.APPLICATION_JSON_VALUE)", "compiler.err.prob.found.req"),
                new Case("BuilderString", "contentType(MediaType.APPLICATION_JSON_VALUE)",
                        "contentType(MediaType.APPLICATION_JSON)", "compiler.err.prob.found.req"),
                new Case("AnnotationToString", "MediaType.APPLICATION_JSON.toString()",
                        "MediaType.APPLICATION_JSON_VALUE",
                        "compiler.err.expression.not.allowable.as.annotation.value"));
    }

    @ParameterizedTest(name = "compile negative -> fixed: {0}")
    @MethodSource("cases")
    void oneChangeFixesTheObservedCompilerFailure(Case sample) throws IOException {
        Path broken = Path.of("examples/broken", sample.name() + ".java");
        Path fixed = Path.of("examples/fixed", sample.name() + ".java");
        String before = Files.readString(broken, StandardCharsets.UTF_8);
        String after = Files.readString(fixed, StandardCharsets.UTF_8);
        assertTrue(before.contains(sample.before()));
        assertEquals(after, before.replace(sample.before(), sample.after()));
        Compilation bad = compile(broken, classes.resolve("broken-" + sample.name()));
        assertFalse(bad.success());
        assertEquals(1, bad.errors().size());
        assertEquals(sample.errorCode(), bad.errors().get(0).getCode());
        bad.errors().forEach(error -> System.out.printf(
                "COMPILER %s line=%d column=%d code=%s message=%s%n",
                sample.name(), error.getLineNumber(), error.getColumnNumber(),
                error.getCode(), error.getMessage(Locale.ENGLISH).replace('\n', ' ')));
        Compilation good = compile(fixed, classes.resolve("fixed-" + sample.name()));
        assertTrue(good.success(), () -> good.errors().toString());
        System.out.println("FIXED " + sample.name() + " compiled=true");
    }

    record Compilation(boolean success, List<Diagnostic<? extends JavaFileObject>> errors) {}

    private static Compilation compile(Path file, Path destination) throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "JDK 17 is required; a JRE alone cannot run these tests");
        Files.createDirectories(destination);
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try (var manager = compiler.getStandardFileManager(diagnostics, Locale.ENGLISH,
                StandardCharsets.UTF_8)) {
            var units = manager.getJavaFileObjects(file);
            boolean success = compiler.getTask(null, manager, diagnostics,
                    List.of("--release", "17", "-encoding", "UTF-8", "-classpath",
                            System.getProperty("java.class.path"), "-d", destination.toString()),
                    null, units).call();
            return new Compilation(success, diagnostics.getDiagnostics().stream()
                    .filter(error -> error.getKind() == Diagnostic.Kind.ERROR).toList());
        }
    }
}
