import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

abstract class AbstractApi {
    private final URL baseUrl;

    AbstractApi(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    enum RetryPolicy {
        STOP, RETRY_ONCE
    }

    record Request(String method, String path, String body, Map<String, String> headers) {}
    record Response(int status, String body) {}

    static final class ResponseTimeout extends IOException {
        ResponseTimeout(SocketTimeoutException cause) {
            super("Timed out while reading the HTTP response", cause);
        }
    }

    protected Response send(Request request, RetryPolicy policy) throws IOException {
        try {
            return sendOnce(request);
        } catch (ResponseTimeout timeout) {
            if (policy == RetryPolicy.STOP) {
                throw timeout;
            }
            return sendOnce(request);
        }
    }

    private Response sendOnce(Request request) throws IOException {
        var connection = (HttpURLConnection) new URL(baseUrl, request.path()).openConnection();
        try {
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(500);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod(request.method());
            request.headers().forEach(connection::setRequestProperty);

            if (request.body() != null) {
                byte[] bytes = request.body().getBytes(StandardCharsets.UTF_8);
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(bytes.length);
                try (var body = connection.getOutputStream()) {
                    body.write(bytes);
                }
            } else {
                connection.connect();
            }

            try {
                int status = connection.getResponseCode();
                try (var body = status >= 400 ? connection.getErrorStream()
                                               : connection.getInputStream()) {
                    return new Response(status, body == null ? ""
                            : new String(body.readAllBytes(), StandardCharsets.UTF_8));
                }
            } catch (SocketTimeoutException timeout) {
                throw new ResponseTimeout(timeout);
            }
        } finally {
            connection.disconnect();
        }
    }
}
