import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrayComparisonTest {

    @Test
    void assertEqualsFailsButAssertArrayEqualsPasses() {
        int[] expected = {1, 2};
        int[] actual = {1, 2};

        // Before: 要素が同じでも、別配列へのassertEqualsは失敗する
        assertThrows(AssertionFailedError.class,
                () -> assertEquals(expected, actual));

        // After: 同じ入力を配列専用のアサーションで比較する
        assertArrayEquals(expected, actual);
    }

    @Test
    void differentOrderIsDetected() {
        assertThrows(AssertionFailedError.class,
                () -> assertArrayEquals(
                        new int[]{1, 2}, new int[]{2, 1}));
    }

    @Test
    void differentLengthIsDetected() {
        assertThrows(AssertionFailedError.class,
                () -> assertArrayEquals(
                        new int[]{1, 2}, new int[]{1, 2, 3}));
    }

    @Test
    void nestedArraysAreComparedByContents() {
        int[][] expected = {{1, 2}, {3, 4}};
        int[][] actual = {{1, 2}, {3, 4}};

        assertFalse(Arrays.equals(expected, actual));
        assertTrue(Arrays.deepEquals(expected, actual));
        assertArrayEquals(expected, actual);

        assertThrows(AssertionFailedError.class,
                () -> assertArrayEquals(
                        expected, new int[][]{{1, 2}, {3, 5}}));
    }
}
