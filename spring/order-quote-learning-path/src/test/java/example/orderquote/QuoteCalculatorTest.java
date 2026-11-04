package example.orderquote;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuoteCalculatorTest {
    private final QuoteCalculator calculator = new QuoteCalculator();

    @Test
    void totalsTwoLines() {
        assertEquals(2750L, calculator.total(List.of(new LineItem(2, 1200), new LineItem(1, 350))));
    }

    @Test
    void totalCanExceedTheIntRange() {
        List<LineItem> lines = Collections.nCopies(50, new LineItem(100, 1_000_000));
        assertEquals(5_000_000_000L, calculator.total(lines));
    }

    @Test
    void acceptsOneFreeLine() {
        assertEquals(0L, calculator.total(List.of(new LineItem(1, 0))));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "0 | 350 | 数量は1〜100です",
            "101 | 350 | 数量は1〜100です",
            "1 | -1 | 単価は0〜1,000,000円です",
            "1 | 1000001 | 単価は0〜1,000,000円です"
    })
    void rejectsAnInvalidLine(int quantity, int unitPrice, String message) {
        var error = assertThrows(InvalidQuoteException.class, () -> new LineItem(quantity, unitPrice));
        assertEquals(message, error.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 51})
    void rejectsAnInvalidNumberOfLines(int count) {
        var lines = Collections.nCopies(count, new LineItem(1, 350));
        var error = assertThrows(InvalidQuoteException.class, () -> calculator.total(lines));
        assertEquals("明細は1〜50件です", error.getMessage());
    }
}
