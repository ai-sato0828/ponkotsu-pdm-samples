import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.startup.Tomcat;

/** Local demonstration only. Both URLs use the very same immutable PDF input. */
public final class PdfServer implements AutoCloseable {
    private final Tomcat tomcat;
    private final Path base;

    private PdfServer(Tomcat tomcat, Path base) {
        this.tomcat = tomcat;
        this.base = base;
    }

    public static byte[] fixture() throws IOException {
        try (InputStream input = PdfServer.class.getResourceAsStream("/sample.pdf")) {
            if (input == null) throw new IOException("sample.pdf is missing");
            return input.readAllBytes();
        }
    }

    public static PdfServer start(int port) throws Exception {
        byte[] pdf = fixture();
        Path base = Files.createTempDirectory("pdf-response-");
        Tomcat tomcat = new Tomcat();
        PdfServer server = new PdfServer(tomcat, base);
        try {
            tomcat.setBaseDir(base.toString());
            tomcat.setPort(port);
            tomcat.getConnector().setProperty("address", "127.0.0.1");
            var context = tomcat.addContext("", base.toString());
            Tomcat.addServlet(context, "pdf", new HttpServlet() {
                @Override
                protected void doGet(HttpServletRequest request, HttpServletResponse response)
                        throws IOException {
                    // /preview and /download differ only in the disposition argument.
                    PdfResponse.send(response, pdf, "/preview".equals(request.getServletPath()));
                }
            });
            context.addServletMappingDecoded("/preview", "pdf");
            context.addServletMappingDecoded("/download", "pdf");
            tomcat.start();
            if (server.port() <= 0) throw new IOException("Loopback HTTP connector did not bind; inspect Tomcat startup log");
            return server;
        } catch (Exception ex) {
            try { server.close(); } catch (Exception cleanup) { ex.addSuppressed(cleanup); }
            throw ex;
        }
    }

    public int port() { return tomcat.getConnector().getLocalPort(); }

    @Override
    public void close() throws Exception {
        try { tomcat.stop(); } finally {
            tomcat.destroy();
            try (var paths = Files.walk(base)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        PdfServer server = start(port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { server.close(); } catch (Exception ex) { ex.printStackTrace(); }
        }));
        System.out.println("Preview: http://127.0.0.1:" + server.port() + "/preview");
        System.out.println("Download: http://127.0.0.1:" + server.port() + "/download");
        server.tomcat.getServer().await();
    }
}
