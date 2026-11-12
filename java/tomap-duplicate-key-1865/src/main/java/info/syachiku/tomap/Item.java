package info.syachiku.tomap;

import java.util.Objects;

public record Item(String code, String label) {
    public Item {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(label, "label");
    }
}
