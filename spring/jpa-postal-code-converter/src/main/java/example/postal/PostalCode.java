package example.postal;

import java.util.Objects;

public record PostalCode(String value) {
    public PostalCode {
        Objects.requireNonNull(value, "value");
        if (!value.matches("[0-9]{7}")) {
            throw new IllegalArgumentException("郵便番号はASCII数字7桁で指定してください");
        }
    }
}
