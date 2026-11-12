package info.syachiku.hashmap;

import java.util.Objects;

public record User(long id, String name) {
    public User {
        Objects.requireNonNull(name, "name");
    }
}
