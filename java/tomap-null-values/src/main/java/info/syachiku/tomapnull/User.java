package info.syachiku.tomapnull;

import java.util.Objects;

/** IDは必須。未設定の表示名はnullで表す。 */
// BEGIN user-model
public record User(String id, String name) {
    public User {
        Objects.requireNonNull(id, "id");
    }
}
// END user-model
