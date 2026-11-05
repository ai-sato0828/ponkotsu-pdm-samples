import static org.junit.jupiter.api.Assertions.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogoutHttpTest {
    private CookieDemoServer server;
    private HttpClient client;

    @BeforeEach
    void startServer() throws Exception {
        server = CookieDemoServer.start(0);
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @AfterEach
    void stopServer() throws Exception {
        if (server != null) server.close();
    }

    @Test
    void checkingWithoutASessionReturns401AndDoesNotIssueACookie() throws Exception {
        var response = get("/protected", null);
        assertEquals(401, response.statusCode());
        assertTrue(response.headers().allValues("Set-Cookie").isEmpty());
        assertEquals(0, server.createdSessions());
    }

    @Test
    void logoutWithoutASessionExpiresTheCookieButCreatesNoSession() throws Exception {
        var response = post("/logout", null);
        assertEquals(204, response.statusCode());
        assertExpirationHeader(response);
        assertEquals(0, server.createdSessions());
        assertEquals(0, server.destroyedSessions());
    }

    @Test
    void oldSessionIdCannotAccessTheProtectedResourceAfterLogout() throws Exception {
        var created = post("/session", null);
        assertEquals(201, created.statusCode());
        String issued = created.headers().firstValue("Set-Cookie").orElseThrow();
        assertTrue(issued.contains("Secure"));
        String sessionPair = issued.split(";", 2)[0];
        assertTrue(sessionPair.startsWith("JSESSIONID="));
        assertEquals(200, get("/protected", sessionPair).statusCode());

        var loggedOut = post("/logout", sessionPair);
        assertEquals(204, loggedOut.statusCode());
        assertExpirationHeader(loggedOut);
        assertEquals(401, get("/protected", sessionPair).statusCode());
        assertEquals(1, server.createdSessions());
        assertEquals(1, server.destroyedSessions());

        // Resending an already invalidated ID must not create a replacement session.
        var secondLogout = post("/logout", sessionPair);
        assertEquals(204, secondLogout.statusCode());
        assertExpirationHeader(secondLogout);
        assertEquals(1, server.createdSessions());
        assertEquals(1, server.destroyedSessions());
        System.out.println("session HTTP: 201 -> protected 200 -> logout 204 -> old ID 401; created=1 destroyed=1");
    }

    private HttpResponse<String> get(String path, String cookie) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(server.uri(path)).timeout(Duration.ofSeconds(5));
        if (cookie != null) builder.header("Cookie", cookie);
        return client.send(builder.GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String cookie) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(server.uri(path)).timeout(Duration.ofSeconds(5));
        if (cookie != null) builder.header("Cookie", cookie);
        return client.send(builder.POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void assertExpirationHeader(HttpResponse<?> response) {
        var headers = response.headers().allValues("Set-Cookie");
        assertEquals(1, headers.size(), "one expiry cookie, no fresh session cookie");
        String header = headers.get(0);
        assertTrue(header.startsWith("JSESSIONID=;"));
        Set<String> attributes = Arrays.stream(header.split(";"))
            .map(String::trim).map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        assertTrue(attributes.contains("path=/"));
        assertTrue(attributes.contains("max-age=0"));
        assertTrue(attributes.contains("secure"));
        assertTrue(attributes.contains("httponly"));
        assertFalse(attributes.stream().anyMatch(s -> s.startsWith("domain=")));
    }
}
