package info.syachiku.finallyreturn;

import java.util.List;
import java.util.Objects;

public final class FinallyExamples {
    private FinallyExamples() {
    }

    // Xクイズと同じgetValue。意図的な不良例。
    public static String getValue() {
        try {
            return "A";
        } finally {
            return "B";
        }
    }

    public static String getValueAfterCleanup(List<String> events) {
        Objects.requireNonNull(events, "events");
        try {
            return "A";
        } finally {
            events.add("cleanup");
        }
    }
}
