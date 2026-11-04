package example.bigdecimal;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class RateCalculator {
    public BigDecimal rate(BigDecimal completed, BigDecimal total) {
        if (total.signum() == 0) {
            throw new IllegalArgumentException("total must not be zero");
        }
        return completed.divide(total, 4, RoundingMode.HALF_UP);
    }
}
