import java.util.ArrayList;
import java.util.List;

public class PeekCountDemo {
    public static void main(String[] args) {
        // BEGIN count
        List<String> observed = new ArrayList<>();
        long count = List.of("A", "B").stream()
                .peek(observed::add)
                .count();

        System.out.println(count);
        System.out.println(observed);
        // END count

        if (count != 2) {
            throw new AssertionError("wrong count");
        }
        List<String> recorded = new ArrayList<>();
        for (String value : List.of("A", "B")) {
            recorded.add(value);
        }
        if (!recorded.equals(List.of("A", "B"))) {
            throw new AssertionError("recording did not reach every value");
        }
        System.out.println("explicit loop: " + recorded);
    }
}
