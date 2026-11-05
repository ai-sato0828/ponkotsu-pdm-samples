import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.OptionalInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PriceLogDemo {
    interface PricingClient {
        int findPrice(String productId) throws SocketTimeoutException;
    }

    interface PriceCache {
        OptionalInt findFresh(String productId);
    }

    record PriceReply(int status, OptionalInt price) {
        static PriceReply ok(int price) {
            return new PriceReply(200, OptionalInt.of(price));
        }

        static PriceReply unavailable() {
            return new PriceReply(503, OptionalInt.empty());
        }
    }

    record PriceLookup(PricingClient pricingClient, PriceCache priceCache, Logger log) {
        // excerpt:lookup:start
        int findPrice(String productId) throws SocketTimeoutException {
            try {
                return pricingClient.findPrice(productId);
            } catch (SocketTimeoutException e) {
                OptionalInt cachedPrice = priceCache.findFresh(productId);
                if (cachedPrice.isEmpty()) {
                    throw e;
                }
                log.warn(
                        "価格APIのタイムアウト後にキャッシュ値を返します productId={}",
                        productId);
                return cachedPrice.getAsInt();
            }
        }
        // excerpt:lookup:end
    }

    record PriceBoundary(PriceLookup lookup, Logger log) {
        // excerpt:boundary:start
        PriceReply handlePrice(String requestId, String productId) {
            try {
                return PriceReply.ok(lookup.findPrice(productId));
            } catch (SocketTimeoutException e) {
                log.error(
                        "価格を取得できませんでした requestId={} productId={}",
                        requestId, productId, e);
                return PriceReply.unavailable();
            }
        }
        // excerpt:boundary:end
    }

    public static void main(String[] args) {
        var log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("price-log-demo");
        log.setLevel(Level.INFO);
        log.setAdditive(false);
        var capture = new ListAppender<ILoggingEvent>();
        capture.start();
        log.addAppender(capture);
        try {
            PricingClient success = productId -> 1200;
            PricingClient timeout = productId -> {
                throw new SocketTimeoutException("price API timed out");
            };
            check("live price", log, capture, success, OptionalInt.of(1100), 200, OptionalInt.of(1200), List.of());
            check("timeout, fresh cache", log, capture, timeout, OptionalInt.of(1100), 200, OptionalInt.of(1100), List.of(Level.WARN));
            check("timeout, no usable cache", log, capture, timeout, OptionalInt.empty(), 503, OptionalInt.empty(), List.of(Level.ERROR));
        } finally {
            log.detachAppender(capture);
            capture.stop();
        }
    }

    private static void check(
            String name, Logger log, ListAppender<ILoggingEvent> capture,
            PricingClient client, OptionalInt cache, int expectedStatus, OptionalInt expectedPrice,
            List<Level> expectedLevels) {
        capture.list.clear();
        PriceBoundary boundary = new PriceBoundary(new PriceLookup(client, productId -> cache, log), log);
        PriceReply actual = boundary.handlePrice("r-1", "p-7");
        List<Level> actualLevels = capture.list.stream().map(ILoggingEvent::getLevel).toList();
        if (actual.status() != expectedStatus || !actual.price().equals(expectedPrice)
                || !actualLevels.equals(expectedLevels)) {
            throw new AssertionError(name + ": " + actual + ", levels=" + actualLevels);
        }
        if (expectedLevels.equals(List.of(Level.ERROR))) {
            ILoggingEvent error = capture.list.get(0);
            if (error.getThrowableProxy() == null
                    || !error.getThrowableProxy().getClassName().equals(SocketTimeoutException.class.getName())
                    || !error.getFormattedMessage().contains("requestId=r-1 productId=p-7")) {
                throw new AssertionError("Missing timeout or request context");
            }
        }
        System.out.printf("%s: status=%d, price=%s, levels=%s%n", name, actual.status(), actual.price(), actualLevels);
    }
}
