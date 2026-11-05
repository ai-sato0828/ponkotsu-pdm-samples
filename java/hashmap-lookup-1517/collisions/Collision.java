import java.util.*;

public class Collision {
    public static void main(String[] args) {
        Map<String, String> names = new HashMap<>();
        names.put("Aa", "first");
        names.put("BB", "second");
        if ("Aa".hashCode() != 2112 || "BB".hashCode() != 2112
                || "Aa".equals("BB") || names.size() != 2
                || !"first".equals(names.get("Aa")) || !"second".equals(names.get("BB"))) {
            throw new AssertionError("hash collision check failed");
        }
        System.out.println("2112: " + names.get("Aa") + ", " + names.get("BB"));
    }
}
