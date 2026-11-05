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
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserId other)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }
}
