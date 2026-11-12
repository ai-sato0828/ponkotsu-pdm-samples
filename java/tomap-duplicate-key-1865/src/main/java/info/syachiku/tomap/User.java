package info.syachiku.tomap;

import java.util.Objects;

/** 数値IDから表示名を引く例。社員番号の例にはEmployeeを使う。 */
public final class User {
    private final Long id;
    private final String name;

    public User(Long id, String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
