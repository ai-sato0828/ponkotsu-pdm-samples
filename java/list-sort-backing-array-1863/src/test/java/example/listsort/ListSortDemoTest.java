package example.listsort;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class ListSortDemoTest {
    private static final List<String> IMPORT_ORDER = List.of("ORD-103", "ORD-101", "ORD-102");
    private static final List<String> DISPLAY_ORDER = List.of("ORD-101", "ORD-102", "ORD-103");

    @Test
    void sortingTheFixedSizeViewChangesTheSourceArray() {
        String[] imported = IMPORT_ORDER.toArray(String[]::new);
        List<String> sorted = ListSortDemo.sortInPlace(Arrays.asList(imported));

        assertEquals(DISPLAY_ORDER, sorted);
        assertArrayEquals(DISPLAY_ORDER.toArray(String[]::new), imported);
    }

    @Test
    void listOfRejectsSortingAndKeepsTheOrder() {
        List<String> readOnly = List.of("ORD-103", "ORD-101", "ORD-102");

        assertThrowsExactly(UnsupportedOperationException.class,
                () -> ListSortDemo.sortInPlace(readOnly));
        assertEquals(IMPORT_ORDER, readOnly);
    }

    @Test
    void sortingACopyKeepsTheSourceArrayInImportOrder() {
        String[] imported = IMPORT_ORDER.toArray(String[]::new);
        List<String> preview = ListSortDemo.sortedCopy(Arrays.asList(imported));

        assertEquals(DISPLAY_ORDER, preview);
        assertArrayEquals(IMPORT_ORDER.toArray(String[]::new), imported);
    }
}
