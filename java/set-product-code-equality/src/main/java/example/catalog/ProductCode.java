package example.catalog;

import java.util.Objects;

public final class ProductCode {
    private final String value;
    private final int displayOrder;

    public ProductCode(String value, int displayOrder) {
        this.value = Objects.requireNonNull(value);
        this.displayOrder = displayOrder;
    }

    public int displayOrder() {
        return displayOrder;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProductCode other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
