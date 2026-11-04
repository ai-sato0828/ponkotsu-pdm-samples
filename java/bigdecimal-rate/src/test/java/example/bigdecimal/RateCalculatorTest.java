package example.bigdecimal;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateCalculatorTest {
    private final RateCalculator calculator = new RateCalculator();

    @Test
    void 端数を小数2桁で四捨五入する() {
        BigDecimal actual = new BigDecimal("1")
                .divide(new BigDecimal("6"), 2, RoundingMode.HALF_UP);

        assertEquals(0, new BigDecimal("0.17").compareTo(actual));
    }

    @Test
    void 達成率を小数4桁で四捨五入する() {
        BigDecimal actual = calculator.rate(new BigDecimal("2"), new BigDecimal("3"));

        assertEquals(new BigDecimal("0.6667"), actual);
    }

    @Test
    void 総件数が正で完了件数が0なら達成率は0() {
        BigDecimal actual = calculator.rate(BigDecimal.ZERO, new BigDecimal("3"));

        assertEquals(new BigDecimal("0.0000"), actual);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void 総件数が0なら未計算として例外にする(int completed) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> calculator.rate(BigDecimal.valueOf(completed), BigDecimal.ZERO));

        assertEquals("total must not be zero", exception.getMessage());
    }
}
