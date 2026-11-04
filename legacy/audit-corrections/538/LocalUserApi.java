import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

final class LocalUserApi implements AutoCloseable {
    private record Registration(String name, int id) {}

    private final AtomicInteger requests = new AtomicInteger();
    private final CountDownLatch releaseFirstRequest = new CountDownLatch(1);
    private final Map<String, Registration> registrationsByKey = new HashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final HttpServer server;
    private final boolean suppressFirstResponse;
    private int savedUsers;

    LocalUserApi(boolean suppressFirstResponse) throws IOException {
        this.suppressFirstResponse = suppressFirstResponse;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(executor);
        server.createContext("/users", this::handle);
        server.start();
    }

    URL baseUrl() throws IOException {
        return new URL("http://127.0.0.1:" + server.getAddress().getPort());
    }

    int requests() {
        return requests.get();
    }

    synchronized int savedUsers() {
        return savedUsers;
    }

    private void handle(HttpExchange exchange) throws IOException {
        int attempt = requests.incrementAndGet();
        try (exchange) {
            AbstractApi.Response response = switch (exchange.getRequestMethod()) {
                case "GET" -> new AbstractApi.Response(200, Integer.toString(savedUsers()));
                case "POST" -> {
                    String name = new String(exchange.getRequestBody().readAllBytes(),
                            StandardCharsets.UTF_8);
                    yield register(name, exchange.getRequestHeaders().getFirst("Idempotency-Key"));
                }
                default -> new AbstractApi.Response(405, "method not allowed");
            };
            if (suppressFirstResponse && attempt == 1) {
                try {
                    releaseFirstRequest.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return;
            }
            byte[] bytes = response.body().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(response.status(), bytes.length);
            exchange.getResponseBody().write(bytes);
        }
    }

    private synchronized AbstractApi.Response register(String name, String key) {
        Registration prior = registrationsByKey.get(key);
        if (prior != null) {
            if (!prior.name().equals(name)) {
                return new AbstractApi.Response(409, "same key with different name");
            }
            return new AbstractApi.Response(201, Integer.toString(prior.id()));
        }
        int id = ++savedUsers;
        if (key != null) {
            registrationsByKey.put(key, new Registration(name, id));
        }
        return new AbstractApi.Response(201, Integer.toString(id));
    }

    @Override
    public void close() {
        releaseFirstRequest.countDown();
        server.stop(0);
        executor.shutdownNow();
    }
}
