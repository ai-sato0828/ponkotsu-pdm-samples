import java.util.HashMap;
import java.util.Map;

public class PutIfAbsentDemo {
    public static void main(String[] args) {
        Map<String, Integer> map = new HashMap<>();
        map.put("A", null);
        map.put("C", 5);
        map.put("D", null);

        System.out.println("A: " + map.putIfAbsent("A", 1) + " -> " + map.get("A"));
        System.out.println("B: " + map.putIfAbsent("B", 1) + " -> " + map.get("B"));
        System.out.println("C: " + map.putIfAbsent("C", 1) + " -> " + map.get("C"));

        if (!map.containsKey("D")) {
            map.put("D", 1);
        }
        System.out.println("D: " + map.containsKey("D") + " -> " + map.get("D"));
    }
}
