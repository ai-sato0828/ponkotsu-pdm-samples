package example;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderRangePreviewTest {
    private final ArrayList<String> orders = new ArrayList<>(List.of("A01", "B02"));

    @Test
    void arrayListAcceptsSizeAsTheExclusiveEnd() {
        assertEquals(List.of("A01", "B02"), orders.subList(0, 2));
    }

    @Test
    void arrayListReturnsAnEmptyViewWhenBothEndpointsEqualSize() {
        assertTrue(orders.subList(2, 2).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"-1, 1", "0, 3"})
    void arrayListRejectsAnEndpointOutsideTheRange(int from, int to) {
        assertThrowsExactly(IndexOutOfBoundsException.class, () -> orders.subList(from, to));
    }

    @Test
    void arrayListRejectsReversedEndpointsWithIllegalArgumentException() {
        assertThrowsExactly(IllegalArgumentException.class, () -> orders.subList(2, 1));
    }

    @Test
    void previewReturnsBothOrdersThroughTheExclusiveEnd() {
        assertEquals(List.of("A01", "B02"), OrderRangePreview.preview(orders, 0, 2));
    }

    @Test
    void previewKeepsAnEmptyRangeAtTheEnd() {
        assertTrue(OrderRangePreview.preview(orders, 2, 2).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"-1, 1", "0, 3", "2, 1"})
    void previewReportsBothEndpointsAndSizeOnInvalidInput(int from, int to) {
        var error = assertThrowsExactly(IllegalArgumentException.class,
                () -> OrderRangePreview.preview(orders, from, to));
        assertEquals("expected 0 <= from <= to <= size (from=%d, to=%d, size=2)"
                .formatted(from, to), error.getMessage());
    }
}
