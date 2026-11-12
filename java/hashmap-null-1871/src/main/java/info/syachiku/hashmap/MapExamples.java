package info.syachiku.hashmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MapExamples {
    private MapExamples() {
    }

    public static Map<String, Integer> scoreFixture() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Alice", null);
        scores.put("Carol", 80);
        return scores;
    }

    public static String describeScore(Map<String, Integer> scores, String name) {
        Objects.requireNonNull(scores, "scores");
        Objects.requireNonNull(name, "name");
        Integer score = scores.get(name);
        if (score != null) {
            return "点数: " + score;
        }
        if (scores.containsKey(name)) {
            return "採点前";
        }
        return "受験者なし";
    }

    public static Map<String, String> labelFixture() {
        Map<String, String> labels = new HashMap<>();
        labels.put("A", null);
        labels.put("C", "登録値");
        return labels;
    }

    public static String defaultIfNull(Map<String, String> labels, String code) {
        String label = Objects.requireNonNull(labels, "labels")
                .get(Objects.requireNonNull(code, "code"));
        if (label == null) {
            label = "未設定";
        }
        return label;
    }

    public static Map<String, Integer> gradedOnly(String name, Integer score) {
        Objects.requireNonNull(name, "name");
        Map<String, Integer> scores = new HashMap<>();
        if (score != null) {
            scores.put(name, score);
        }
        return scores;
    }

    public static void putNullIntoConcurrentMap() {
        Map<String, String> map = new ConcurrentHashMap<>();
        map.put("A", null);
    }

    public static void putNullIntoMapOf() {
        Map.of("A", (String) null);
    }

    public static KeyObservation identityKeyObservation() {
        Map<Keys.IdentityKey, String> users = new HashMap<>();
        Keys.IdentityKey inserted = new Keys.IdentityKey("E001");
        users.put(inserted, "Alice");
        Keys.IdentityKey sameValue = new Keys.IdentityKey("E001");
        return new KeyObservation(users.get(sameValue),
                users.containsKey(sameValue), users.size());
    }

    public static KeyObservation mutableKeyObservation() {
        Map<Keys.MutableKey, String> users = new HashMap<>();
        Keys.MutableKey key = new Keys.MutableKey("E001");
        users.put(key, "Alice");
        key.setEmployeeNo("E002");
        return new KeyObservation(users.get(key), users.containsKey(key), users.size());
    }

    public static KeyObservation immutableKeyObservation() {
        Map<Keys.EmployeeKey, String> users = new HashMap<>();
        users.put(new Keys.EmployeeKey("E001"), "Alice");
        Keys.EmployeeKey sameValue = new Keys.EmployeeKey("E001");
        return new KeyObservation(users.get(sameValue),
                users.containsKey(sameValue), users.size());
    }

    public record KeyObservation(String value, boolean present, int size) {
    }
}
