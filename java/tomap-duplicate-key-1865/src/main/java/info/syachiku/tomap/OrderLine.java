package info.syachiku.tomap;

import java.util.Objects;

public record OrderLine(String productCode, int quantity) {
    public OrderLine {
        Objects.requireNonNull(productCode, "productCode");
    }
}
