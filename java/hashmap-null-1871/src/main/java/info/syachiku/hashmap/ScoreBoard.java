package info.syachiku.hashmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ScoreBoard {
    private final Map<String, ScoreResult> results = new HashMap<>();

    public void register(String name, ScoreResult result) {
        results.put(Objects.requireNonNull(name, "name"),
                Objects.requireNonNull(result, "result"));
    }

    public String describe(String name) {
        ScoreResult result = results.get(Objects.requireNonNull(name, "name"));
        if (result == null) {
            return "受験者なし";
        }
        if (result.status() == ScoreStatus.NOT_GRADED) {
            return "採点前";
        }
        return "点数: " + result.score();
    }
}
