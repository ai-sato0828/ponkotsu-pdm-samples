import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

/** Article 1095's cookie factories, exercised through a real Servlet container. */
public final class CookieDemoServer implements AutoCloseable {
    private final Tomcat tomcat;
    private final Path baseDir;
    private final SessionCounter sessions;

    private CookieDemoServer(Tomcat tomcat, Path baseDir, SessionCounter sessions) {
        this.tomcat = tomcat;
        this.baseDir = baseDir;
        this.sessions = sessions;
    }

    public static CookieDemoServer start(int port) throws Exception {
        Path baseDir = Files.createTempDirectory("servlet-cookie-factory-");
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toString());
        tomcat.setPort(port);
        tomcat.getConnector().setProperty("address", "127.0.0.1");

        Context context = tomcat.addContext("", baseDir.toString());
        context.setSessionCookiePath("/");
        context.setUseHttpOnly(true);
        SessionCounter sessions = new SessionCounter();
        context.addServletContainerInitializer((classes, servletContext) -> {
            servletContext.getSessionCookieConfig().setSecure(true);
            servletContext.addListener(sessions);
        }, java.util.Set.of());
        Tomcat.addServlet(context, "cookie-demo", new DemoServlet());
        context.addServletMappingDecoded("/*", "cookie-demo");
        try {
            tomcat.start();
            if (tomcat.getConnector().getLocalPort() < 1) {
                throw new IOException("The loopback HTTP connector did not start; inspect the preceding Tomcat error.");
            }
            return new CookieDemoServer(tomcat, baseDir, sessions);
        } catch (Exception failure) {
            try {
                tomcat.destroy();
            } catch (Exception cleanup) {
                failure.addSuppressed(cleanup);
            }
            deleteTree(baseDir);
            throw failure;
        }
    }

    public URI uri(String path) {
        return URI.create("http://127.0.0.1:" + tomcat.getConnector().getLocalPort() + path);
    }

    public int createdSessions() {
        return sessions.created.get();
    }

    public int destroyedSessions() {
        return sessions.destroyed.get();
    }

    @Override
    public void close() throws Exception {
        try {
            tomcat.stop();
        } finally {
            try {
                tomcat.destroy();
            } finally {
                deleteTree(baseDir);
            }
        }
    }

    private static void deleteTree(Path directory) throws IOException {
        try (var files = Files.walk(directory)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(file);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        CookieDemoServer server = start(port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.close();
            } catch (Exception e) {
                System.err.println("Demo shutdown: " + e.getMessage());
            }
        }));
        System.out.println("Cookie demo: " + server.uri("/"));
        System.out.println("POST /session; GET /protected; POST /logout");
        System.out.println("Local HTTP observes headers and server sessions. Secure-cookie browser behavior requires HTTPS.");
        server.tomcat.getServer().await();
    }

    private static final class SessionCounter implements HttpSessionListener {
        private final AtomicInteger created = new AtomicInteger();
        private final AtomicInteger destroyed = new AtomicInteger();

        @Override
        public void sessionCreated(HttpSessionEvent event) {
            created.incrementAndGet();
        }

        @Override
        public void sessionDestroyed(HttpSessionEvent event) {
            destroyed.incrementAndGet();
        }
    }

    private static final class DemoServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
            switch (request.getRequestURI()) {
                case "/session" -> {
                    request.getSession(true).setAttribute("user", "demo");
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.setContentType("text/plain;charset=UTF-8");
                    response.getWriter().println("demo session created");
                }
                case "/logout" -> {
                    JSESSIONID.logout(request, response);
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
                default -> response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.setContentType("text/plain;charset=UTF-8");
            if (!request.getRequestURI().equals("/protected")) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            HttpSession session = request.getSession(false);
            if (session == null || !"demo".equals(session.getAttribute("user"))) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().println("no active demo session");
                return;
            }
            response.getWriter().println("demo session active");
        }
    }
}
