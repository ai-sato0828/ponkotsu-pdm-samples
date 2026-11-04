import java.io.IOException;
import java.net.URL;
import java.util.Map;

final class UserRequestService extends AbstractApi {
    UserRequestService(URL baseUrl) {
        super(baseUrl);
    }

    Response findUsers() throws IOException {
        return send(new Request("GET", "/users", null, Map.of()),
                RetryPolicy.RETRY_ONCE);
    }

    Response registerUser(String name, String key) throws IOException {
        boolean hasKey = key != null && !key.isBlank();
        return send(registration(name, hasKey ? key : null),
                hasKey ? RetryPolicy.RETRY_ONCE : RetryPolicy.STOP);
    }

    Response registerWithoutKeyAndRetryForComparison(String name) throws IOException {
        return send(registration(name, null), RetryPolicy.RETRY_ONCE);
    }

    private Request registration(String name, String key) {
        var headers = key == null
                ? Map.of("Content-Type", "text/plain; charset=UTF-8")
                : Map.of("Content-Type", "text/plain; charset=UTF-8", "Idempotency-Key", key);
        return new Request("POST", "/users", name, headers);
    }
}
