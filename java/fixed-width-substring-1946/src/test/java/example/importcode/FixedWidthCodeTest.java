package example.importcode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class FixedWidthCodeTest {
    @ParameterizedTest
    @CsvSource({"AB1234,1234", "AB1234TAIL,1234", "ABwxyz,wxyz"})
    void bothVersionsExtractTheSameFourCharacters(String input, String expected) {
        assertEquals(expected, FixedWidthCode.extractUnchecked(input));
        assertEquals(expected, FixedWidthCode.extractPayload(input));
    }

    @ParameterizedTest
    @ValueSource(strings = "AB123")
    @EmptySource
    void uncheckedVersionFailsInsideSubstringForShortInput(String input) {
        var failure = assertThrows(IndexOutOfBoundsException.class,
                () -> FixedWidthCode.extractUnchecked(input));
        System.out.printf("BEFORE length=%d observed=%s%n", input.length(),
                failure.getClass().getSimpleName());
    }

    @ParameterizedTest
    @ValueSource(strings = "AB123")
    @EmptySource
    void correctedVersionRejectsShortInputWithTheRequiredAndActualLength(String input) {
        var failure = assertThrows(IllegalArgumentException.class,
                () -> FixedWidthCode.extractPayload(input));
        assertEquals("取込コードは6文字以上が必要です: 実際=" + input.length(), failure.getMessage());
    }

    @Test
    void nullIsRejectedByBothVersions() {
        assertThrows(NullPointerException.class, () -> FixedWidthCode.extractUnchecked(null));
        assertThrows(NullPointerException.class, () -> FixedWidthCode.extractPayload(null));
    }

    @Test
    void equalBeginAndEndAtLengthIsAnEmptySubstring() {
        String input = "AB1234";
        assertEquals("", input.substring(input.length(), input.length()));
    }

    @Test
    void endPastLengthIsOutsideTheJdkRange() {
        assertThrows(IndexOutOfBoundsException.class, () -> "AB1234".substring(2, 7));
    }

    @Test
    void reversedRangeIsOutsideTheJdkRange() {
        assertThrows(IndexOutOfBoundsException.class, () -> "AB1234".substring(4, 2));
    }

    @Test
    void negativeBeginIsOutsideTheJdkRange() {
        assertThrows(IndexOutOfBoundsException.class, () -> "AB1234".substring(-1, 2));
    }
}
