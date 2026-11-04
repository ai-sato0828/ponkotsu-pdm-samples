import java.util.Objects;

public class IntegerEqualityDemo {
    public static void main(String[] args) {
        // BEGIN cache
        Integer a = 100;
        Integer b = 100;
        System.out.println(a == b); // true

        Integer x = 1000;
        Integer y = 1000;
        System.out.println(x == y); // JDK 17の既定設定ではfalse
        // END cache

        if (a != b || !Objects.equals(x, y) || Objects.equals(x, null)) {
            throw new AssertionError("expected numeric equality");
        }
        Integer absent = null;
        try {
            boolean same = absent == 1000;
            throw new AssertionError("expected NullPointerException, got " + same);
        } catch (NullPointerException expected) {
            System.out.println("mixed Integer/int with null: NullPointerException");
        }
    }
}
