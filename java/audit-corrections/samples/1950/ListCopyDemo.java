import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListCopyDemo {
    static final class User {
        final int id;
        String name;

        User(int id, String name) {
            this.id = id;
            this.name = name;
        }

        User(User source) {
            this(source.id, source.name);
        }
    }

    public static void main(String[] args) {
        List<User> original = new ArrayList<>(List.of(new User(1, "before")));
        // BEGIN readOnly
        List<User> readOnly = List.copyOf(original);
        try {
            readOnly.add(new User(2, "added"));
            throw new AssertionError("add should be unsupported");
        } catch (UnsupportedOperationException expected) {
            System.out.println("readOnly.add: UnsupportedOperationException");
        }
        readOnly.get(0).name = "shared";
        // END readOnly
        if (!original.get(0).name.equals("shared")) {
            throw new AssertionError("expected shared element");
        }

        // BEGIN copy
        List<User> copied = original.stream()
                .map(User::new)
                .collect(Collectors.toCollection(ArrayList::new));
        // END copy
        copied.get(0).name = "independent";
        if (original.get(0) == copied.get(0)
                || !original.get(0).name.equals("shared")
                || !copied.get(0).name.equals("independent")) {
            throw new AssertionError("expected independent elements");
        }
        System.out.println("original=shared, copied=independent");
    }
}
