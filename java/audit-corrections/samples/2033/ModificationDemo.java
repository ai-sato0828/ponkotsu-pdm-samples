import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class ModificationDemo {
    public static void main(String[] args) {
        // BEGIN failure
        List<String> statuses =
                new ArrayList<>(List.of("ACTIVE", "STOP", "ACTIVE", "ACTIVE"));

        for (String status : statuses) {
            if (status.equals("STOP")) {
                statuses.remove(status);
            }
        }
        // END failure
    }

    static void verify() {
        try {
            main(new String[0]);
            throw new AssertionError("expected ConcurrentModificationException");
        } catch (ConcurrentModificationException expected) {
            System.out.println("4 elements: ConcurrentModificationException");
        }

        // BEGIN skipped
        List<String> shorter = new ArrayList<>(List.of("ACTIVE", "STOP", "ACTIVE"));
        List<String> visited = new ArrayList<>();
        for (String status : shorter) {
            visited.add(status);
            if (status.equals("STOP")) {
                shorter.remove(status);
            }
        }
        // END skipped
        if (!visited.equals(List.of("ACTIVE", "STOP"))) {
            throw new AssertionError(visited.toString());
        }
        System.out.println("3 elements: visited=" + visited);

        List<String> fixed = new ArrayList<>(List.of("ACTIVE", "STOP", "STOP", "ACTIVE"));
        boolean removed = fixed.removeIf("STOP"::equals);
        if (!removed || !fixed.equals(List.of("ACTIVE", "ACTIVE"))) {
            throw new AssertionError(fixed.toString());
        }
        System.out.println("removeIf: " + fixed);
    }
}
