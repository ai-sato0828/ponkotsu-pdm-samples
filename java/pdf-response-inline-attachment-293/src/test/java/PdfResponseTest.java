import static org.junit.jupiter.api.Assertions.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PdfResponseTest {
    private static PdfServer server;
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    @BeforeAll static void start() throws Exception { server = PdfServer.start(0); }
    @AfterAll static void stop() throws Exception { if (server != null) server.close(); }

    @ParameterizedTest
    @CsvSource({"/preview, inline", "/download, attachment"})
    void samePdfWithDifferentDisposition(String path, String disposition) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + server.port() + path))
                .timeout(Duration.ofSeconds(10)).GET().build();
        HttpResponse<byte[]> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        String expected = disposition + "; filename=\"sample.pdf\"";
        assertAll(
                () -> assertEquals(200, response.statusCode()),
                () -> assertEquals("application/pdf", response.headers().firstValue("Content-Type").orElseThrow()),
                () -> assertEquals(expected, response.headers().firstValue("Content-Disposition").orElseThrow()),
                () -> assertArrayEquals(PdfServer.fixture(), response.body())
        );
        String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(response.body()));
        System.out.println(path + " status=" + response.statusCode()
                + " type=application/pdf disposition=" + expected
                + " bytes=" + response.body().length + " sha256=" + sha);
    }
}
