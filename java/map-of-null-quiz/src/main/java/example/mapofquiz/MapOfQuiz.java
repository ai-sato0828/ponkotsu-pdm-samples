package example.mapofquiz;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class MapOfQuiz {
    private MapOfQuiz() {
    }

    static void quiz() {
        Map<String, String> values =
                Map.of("A", null);

        System.out.println(values.get("A"));
    }

    static void explicitNullValue() {
        String value = null;
        Map<String, String> map = Map.of("A", value);
        // ここでNullPointerException
    }

    static Map<String, String> hashMapAllowsNull() {
        Map<String, String> map = new HashMap<>();
        map.put("A", null);

        System.out.println(map.get("A")); // null
        System.out.println(map.get("B")); // null
        return map;
    }

    static Map<String, String> attributesBefore(UserForm userForm) {
        Map<String, String> attributes = Map.of(
                "department", userForm.getDepartment(),
                "extension", userForm.getExtension());
        return attributes;
    }

    static Map<String, String> mutableCopy() {
        Map<String, String> map = new HashMap<>(
                Map.of("status", "NEW"));

        map.put("owner", "U001");
        return map;
    }

    record UserAttribute(String department, Optional<String> extension) {}

    static UserAttribute optionalAttribute(String department, String extension) {
        UserAttribute attribute = new UserAttribute(
                department, Optional.ofNullable(extension));
        return attribute;
    }

    public static void main(String[] args) {
        reportNull("quiz", MapOfQuiz::quiz);
        reportNull("explicit null", MapOfQuiz::explicitNullValue);

        Map<String, String> nullable = hashMapAllowsNull();
        System.out.println("contains A=" + nullable.containsKey("A"));
        System.out.println("contains B=" + nullable.containsKey("B"));

        UserForm withoutExtension = new UserForm("営業", null);
        reportNull("optional form field", () -> attributesBefore(withoutExtension));
        Map<String, String> mapped = new AttributeMapper().toAttributes(withoutExtension);
        System.out.println("mapped department=" + mapped.get("department"));
        System.out.println("mapped extension present=" + mapped.containsKey("extension"));

        Map<String, String> updated = mutableCopy();
        System.out.println("status=" + updated.get("status"));
        System.out.println("owner=" + updated.get("owner"));

        UserAttribute dto = optionalAttribute("営業", null);
        System.out.println("optional extension present=" + dto.extension().isPresent());
    }

    private static void reportNull(String label, Runnable action) {
        try {
            action.run();
        } catch (NullPointerException e) {
            System.out.println(label + ": " + e.getClass().getSimpleName());
        }
    }
}
