import java.util.*;

public class Lookup {
    public static void main(String[] args) {
        Map<UserId, String> names = new HashMap<>();

        names.put(new UserId("U001"), "田中");

        String name = names.get(new UserId("U001"));
        System.out.println(name);
    }
}

final class UserId {
    private final String value;

    public UserId(String value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UserId other)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
