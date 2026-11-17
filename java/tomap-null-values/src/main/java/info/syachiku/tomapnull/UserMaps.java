package info.syachiku.tomapnull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class UserMaps {
    private UserMaps() {
    }

    // BEGIN two-arguments
    public static Map<String, String> toMapTwoArguments(List<User> users) {
        return users.stream()
                .collect(Collectors.toMap(User::id, User::name));
    }
    // END two-arguments

    // BEGIN hash-map-supplier
    public static Map<String, String> toMapWithHashMapSupplier(List<User> users) {
        return users.stream().collect(Collectors.toMap(
                User::id,
                User::name,
                (left, right) -> {
                    throw new IllegalStateException("duplicate id");
                },
                HashMap::new));
    }
    // END hash-map-supplier

    // BEGIN filter-policy
    public static Map<String, String> withoutUnsetNames(List<User> users) {
        return users.stream()
                .filter(user -> user.name() != null)
                .collect(Collectors.toMap(User::id, User::name));
    }
    // END filter-policy

    // BEGIN default-policy
    public static Map<String, String> withDefaultName(List<User> users, String defaultName) {
        Objects.requireNonNull(defaultName, "defaultName");
        return users.stream().collect(Collectors.toMap(
                User::id,
                user -> user.name() == null ? defaultName : user.name()));
    }
    // END default-policy

    // BEGIN preserve-null-fix
    public static Map<String, String> preservingUnsetNames(List<User> users) {
        Map<String, String> namesById = new HashMap<>();
        for (User user : users) {
            if (namesById.containsKey(user.id())) {
                throw new IllegalArgumentException("duplicate id: " + user.id());
            }
            namesById.put(user.id(), user.name());
        }
        return namesById;
    }
    // END preserve-null-fix
}
