package info.syachiku.samples.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class MoneyRules {
    private MoneyRules() {}

    public static boolean sameAmount(BigDecimal left, BigDecimal right) {
        Objects.requireNonNull(left, "left amount is required");
        Objects.requireNonNull(right, "right amount is required");
        return left.compareTo(right) == 0;
    }

    public static BigDecimal fixedTwoDecimals(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount is required");
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static boolean withinLimit(BigDecimal amount, BigDecimal limit) {
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(limit, "limit is required");
        return amount.compareTo(limit) <= 0;
    }
}
