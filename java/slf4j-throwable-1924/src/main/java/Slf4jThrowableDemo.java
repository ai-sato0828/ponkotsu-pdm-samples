import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jThrowableDemo {
    private static final Logger log = LoggerFactory.getLogger(Slf4jThrowableDemo.class);

    static void run(String scenario) {
        String userId = "U001";
        var e = new IllegalStateException("Connection refused",
                new SQLException("sample database unavailable"));

        switch (scenario) {
            case "message-only" -> log.error("Save failed: {}", e.getMessage());
            case "throwable-only" -> log.error("保存に失敗", e);
            case "id-then-throwable" -> log.error("userId={} の保存に失敗", userId, e);
            case "extra-placeholder" -> log.error("userId={} の保存に失敗: {}", userId, e);
            case "throwable-not-last" -> log.error("保存に失敗: {} userId={}", e, userId);
            case "last-is-string" -> log.error("userId={} の保存に失敗: {}", userId, e.getMessage());
            case "lone-throwable-placeholder" -> log.error("保存に失敗: {}", e);
            default -> throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }
    }
}
