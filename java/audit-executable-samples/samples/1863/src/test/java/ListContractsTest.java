import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListContractsTest {
    @Test
    void arrayAndFixedSizeListShareElementsBothWays() {
        String[] array = {"A", "B"};
        List<String> view = Arrays.asList(array);

        view.set(0, "X");
        assertEquals("X", array[0]);
        array[1] = "Y";
        assertEquals(List.of("X", "Y"), view);
        assertThrows(UnsupportedOperationException.class, () -> view.add("C"));
    }

    @Test
    void arrayListCopyCanChangeWithoutChangingOriginalArray() {
        String[] array = {"A", "B"};
        List<String> copy = new ArrayList<>(Arrays.asList(array));

        array[0] = "X";
        copy.set(1, "Y");
        copy.add("C");

        assertEquals(List.of("A", "Y", "C"), copy);
        assertEquals(List.of("X", "B"), Arrays.asList(array));
    }

    @Test
    void listOfRejectsNullAndReplacement() {
        List<String> values = List.of("A", "B");

        assertThrows(NullPointerException.class, () -> List.of("A", null));
        assertThrows(UnsupportedOperationException.class, () -> values.set(0, "X"));
    }
}
