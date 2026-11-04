package info.syachiku.snapshot;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SnapshotPublisherTest {
    private static final byte[] PREVIOUS = "date\tstatus\n2026-11-03\tOK\n".getBytes(UTF_8);
    private static final byte[] UPDATED = "date\tstatus\n2026-11-04\tOK\n".getBytes(UTF_8);
    private static final byte[] PARTIAL = "date\tstatus\n".getBytes(UTF_8);

    @TempDir
    Path directory;

    @Test
    void 直接上書きでは例外の前に既存ファイルが部分出力へ変わる() throws IOException {
        Path target = Files.write(directory.resolve("daily.tsv"), PREVIOUS);

        assertThrows(IOException.class,
                () -> new DirectSnapshotPublisher().publish(target, this::failAfterPartialWrite));

        assertArrayEquals(PARTIAL, Files.readAllBytes(target));
    }

    @Test
    void 一時出力の失敗後も既存ファイルの全バイトが残る() throws IOException {
        Path target = Files.write(directory.resolve("daily.tsv"), PREVIOUS);

        IOException failure = assertThrows(IOException.class,
                () -> new SnapshotPublisher().publish(target, this::failAfterPartialWrite));

        assertEquals("入力の読み込みに失敗", failure.getMessage());
        assertArrayEquals(PREVIOUS, Files.readAllBytes(target));
        assertEquals(Set.of("daily.tsv"), names());
    }

    @Test
    void 初回の一時出力で失敗したら公開ファイルを作らない() throws IOException {
        Path target = directory.resolve("daily.tsv");

        assertThrows(IOException.class,
                () -> new SnapshotPublisher().publish(target, this::failAfterPartialWrite));

        assertFalse(Files.exists(target));
        assertEquals(Set.of(), names());
    }

    @Test
    void 初回の出力が成功すると新規公開し一時ファイルは残らない() throws IOException {
        Path target = directory.resolve("daily.tsv");
        try {
            new SnapshotPublisher().publish(target, output -> output.write(UPDATED));
        } catch (AtomicMoveNotSupportedException unsupported) {
            assumeTrue(false, "この一時ディレクトリではatomic moveが使えない");
        }

        assertArrayEquals(UPDATED, Files.readAllBytes(target));
        assertEquals(Set.of("daily.tsv"), names());
    }

    @Test
    void 既存ファイルを置換できる環境では完成した内容へ入れ替わる() throws IOException {
        Path target = Files.write(directory.resolve("daily.tsv"), PREVIOUS);
        try {
            new SnapshotPublisher().publish(target, output -> output.write(UPDATED));
        } catch (AtomicMoveNotSupportedException | FileAlreadyExistsException unsupported) {
            assumeTrue(false, "この一時ディレクトリでは既存ファイルのatomic置換を確認できない");
        }

        assertArrayEquals(UPDATED, Files.readAllBytes(target));
        assertEquals(Set.of("daily.tsv"), names());
    }

    @Test
    void atomic非対応を模擬したら例外を返し別の上書きを試さない() throws IOException {
        Path target = Files.write(directory.resolve("daily.tsv"), PREVIOUS);
        var publisher = new SnapshotPublisher((staged, destination) -> {
            assertEquals(target, destination);
            assertEquals(target.getParent(), staged.getParent());
            assertArrayEquals(UPDATED, Files.readAllBytes(staged));
            throw new AtomicMoveNotSupportedException(
                    staged.toString(), destination.toString(), "テスト用");
        });

        assertThrows(AtomicMoveNotSupportedException.class,
                () -> publisher.publish(target, output -> output.write(UPDATED)));

        assertArrayEquals(PREVIOUS, Files.readAllBytes(target));
        assertEquals(Set.of("daily.tsv"), names());
    }

    private void failAfterPartialWrite(OutputStream output) throws IOException {
        output.write(PARTIAL);
        throw new IOException("入力の読み込みに失敗");
    }

    private Set<String> names() throws IOException {
        try (var files = Files.list(directory)) {
            return files.map(path -> path.getFileName().toString()).collect(Collectors.toSet());
        }
    }
}
