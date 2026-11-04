package example.paging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class IndexBoundariesTest {
    @Test
    void emptyListHasNoFirstElement() {
        assertThrows(IndexOutOfBoundsException.class, () -> List.of().get(0));
    }

    @Test
    void singletonAcceptsZeroButRejectsSize() {
        List<String> values = List.of("A");
        assertEquals("A", values.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> values.get(values.size()));
    }

    @Test
    void threeItemsAcceptTheLastIndexButRejectNegativeAndSize() {
        List<String> values = List.of("A", "B", "C");
        assertEquals("C", values.get(values.size() - 1));
        assertThrows(IndexOutOfBoundsException.class, () -> values.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> values.get(values.size()));
    }

    @Test
    void arrayAcceptsLastIndexButRejectsLength() {
        String[] values = {"A", "B", "C"};
        assertEquals("C", values[values.length - 1]);
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            String ignored = values[values.length];
        });
    }

    @ParameterizedTest
    @CsvSource({"1, 0", "2, 1", "2147483647, 2147483646"})
    void convertsPositivePageBeforeSearching(int requestedPage, int expectedIndex) {
        assertEquals(expectedIndex, PageIndex.toZeroBased(requestedPage));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void rejectsNonPositivePageBeforeSubtracting(int requestedPage) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> PageIndex.toZeroBased(requestedPage));
        assertEquals("ページ番号は1以上を指定してください", error.getMessage());
    }
}
