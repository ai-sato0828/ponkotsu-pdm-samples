import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ReadTimeoutExample {
    public static void main(String[] args) throws IOException {
        var executor = Executors.newCachedThreadPool();
        var requests = new AtomicInteger();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(executor);
        server.createContext("/", exchange -> {
            try {
                if (requests.incrementAndGet() == 1) {
                    Thread.sleep(750);
                }
                exchange.sendResponseHeaders(200, -1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();

        URL url = new URL("http://127.0.0.1:" + server.getAddress().getPort() + "/");
        HttpURLConnection first = open(url);
        try {
            try {
                first.getResponseCode();
                throw new AssertionError("expected a read timeout");
            } catch (SocketTimeoutException expected) {
                System.out.println("first request -> " + expected.getClass().getSimpleName());
            }

            try {
                System.out.println("same instance -> HTTP " + first.getResponseCode());
            } catch (IOException observed) {
                System.out.println("same instance -> " + observed.getClass().getSimpleName());
            }

            HttpURLConnection retry = open(url);
            try {
                int status = retry.getResponseCode();
                if (status != 200) {
                    throw new AssertionError("expected HTTP 200, got " + status);
                }
                System.out.println("new instance -> HTTP " + status);
            } finally {
                retry.disconnect();
            }
        } finally {
            first.disconnect();
            server.stop(0);
            executor.shutdownNow();
        }
    }

    private static HttpURLConnection open(URL url) throws IOException {
        var connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(250);
        return connection;
    }
}
