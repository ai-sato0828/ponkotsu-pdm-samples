package example.dispatch;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DispatchPreviewTest {
    private static final List<String> PROCESS_ORDER = List.of("O-003", "O-001", "O-002");

    @Test
    void sortingTheViewAlsoReordersTheProcessingQueue() {
        List<String> pending = new ArrayList<>(PROCESS_ORDER);

        List<String> preview = BrokenDispatchPreview.sortedCodes(pending, 0, 2);

        assertEquals(List.of("O-001", "O-003"), preview);
        assertEquals(List.of("O-001", "O-003", "O-002"), pending);
    }

    @Test
    void sortingTheCopyKeepsTheProcessingQueueInOrder() {
        List<String> pending = new ArrayList<>(PROCESS_ORDER);

        List<String> preview = DispatchPreview.sortedCodes(pending, 0, 2);

        assertEquals(List.of("O-001", "O-003"), preview);
        assertEquals(PROCESS_ORDER, pending);
    }

    @Test
    void previewAndQueueCanBeEditedIndependently() {
        List<String> pending = new ArrayList<>(PROCESS_ORDER);
        List<String> preview = DispatchPreview.sortedCodes(pending, 0, 2);

        preview.set(0, "O-009");
        assertEquals(PROCESS_ORDER, pending);

        pending.clear();
        assertEquals(List.of("O-009", "O-003"), preview);
    }

    @Test
    void settingAnElementThroughEitherViewIsReflectedInTheOther() {
        List<String> pending = new ArrayList<>(PROCESS_ORDER);
        List<String> view = pending.subList(0, 2);

        view.set(0, "O-008");
        assertEquals("O-008", pending.get(0));

        pending.set(1, "O-009");
        assertEquals(List.of("O-008", "O-009"), view);
    }

    @Test
    void clearingTheViewRemovesTheSelectedRangeFromTheQueue() {
        List<String> pending = new ArrayList<>(PROCESS_ORDER);

        pending.subList(0, 2).clear();

        assertEquals(List.of("O-002"), pending);
    }

    @Test
    void fromEqualToToIsAnEmptyRangeEvenAtSize() {
        List<String> pending = new ArrayList<>(PROCESS_ORDER);

        assertEquals(List.of(), DispatchPreview.sortedCodes(pending, 3, 3));
    }

    @Test
    void sizeIsAValidExclusiveEnd() {
        List<String> pending = new ArrayList<>(PROCESS_ORDER);

        assertEquals(List.of("O-001", "O-002", "O-003"),
                DispatchPreview.sortedCodes(pending, 0, pending.size()));
    }

    @Test
    void endPastSizeIsRejected() {
        List<String> pending = new ArrayList<>(PROCESS_ORDER);

        assertThrows(IndexOutOfBoundsException.class,
                () -> DispatchPreview.sortedCodes(pending, 0, 4));
    }

    @Test
    void reversedRangeIsRejected() {
        List<String> pending = new ArrayList<>(PROCESS_ORDER);

        assertThrows(IllegalArgumentException.class,
                () -> DispatchPreview.sortedCodes(pending, 2, 1));
    }
}
