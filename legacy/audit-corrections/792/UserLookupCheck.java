import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserLookupCheck {
    public static void main(String[] args) {
        var calls = new ArrayList<String>();
        var lookup = new UserLookup(id -> {
            calls.add(id);
            return id.equals("U-001") ? Optional.of("Aki") : Optional.empty();
        });

        check(lookup.findDisplayName(" U-001 ").equals(Optional.of("Aki")));
        check(lookup.findDisplayName("U-999").isEmpty());

        for (String missing : new String[]{null, "  "}) {
            try {
                lookup.findDisplayName(missing);
                throw new AssertionError("missing ID accepted");
            } catch (IllegalArgumentException expected) {
                check(expected.getMessage().equals("userId is required"));
            }
        }

        check(calls.equals(List.of("U-001", "U-999")));
        System.out.println("5 checks passed");
    }

    private static void check(boolean condition) {
        if (!condition) {
            throw new AssertionError();
        }
    }
}
