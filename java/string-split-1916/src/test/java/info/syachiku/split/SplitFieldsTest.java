package info.syachiku.split;

import java.util.stream.Stream;
import java.util.regex.PatternSyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

class SplitFieldsTest {

    @Test
    void S01_wrongDotDropsAllTrailingEmptyFields() {
        assertArrayEquals(new String[0], SplitFields.wrongDot("a.b", 0));
    }

    @Test
    void S02_wrongDotNegativeLimitShowsFourEmptyFields() {
        assertArrayEquals(new String[] {"", "", "", ""}, SplitFields.wrongDot("a.b", -1));
    }

    @Test
    void S03_escapedDotKeepsTheTwoValues() {
        assertArrayEquals(new String[] {"a", "b"}, SplitFields.literalDot("a.b", 0));
    }

    @Test
    void S04_positiveLimitTwoKeepsRemainderInLastField() {
        assertArrayEquals(new String[] {"A", "B.C"}, SplitFields.literalDot("A.B.C", 2));
    }

    @Test
    void S05_positiveLimitOneDoesNotSplit() {
        assertArrayEquals(new String[] {"A.B.C"}, SplitFields.literalDot("A.B.C", 1));
    }

    @Test
    void S06_largePositiveLimitKeepsTrailingEmptyField() {
        assertArrayEquals(new String[] {"A", "B", ""}, SplitFields.literalDot("A.B.", 10));
    }

    @Test
    void S07_displayDistinguishesEmptyArrayAndOneEmptyString() {
        assertEquals("length=0 values=[]", SplitFields.describe(new String[0]));
        assertEquals("length=1 values=[\"\"]", SplitFields.describe(new String[] {""}));
    }

    @Test
    void S08_nullInputIsNotAnEmptyString() {
        assertThrows(NullPointerException.class, () -> SplitFields.literalDot(null, -1));
        assertThrows(NullPointerException.class, () -> SplitFields.literalDelimiter(null, ".", -1));
    }

    @Test
    void S09_nullDelimiterIsRejected() {
        assertThrows(NullPointerException.class, () -> SplitFields.literalDelimiter("A.B", null, -1));
    }

    @Test
    void S10_emptyDelimiterIsInvalidConfigurationForThisHelper() {
        assertThrows(IllegalArgumentException.class, () -> SplitFields.literalDelimiter("AB", "", -1));
    }

    @Test
    void S11_unquotedPlusIsNotALiteralDelimiter() {
        assertThrows(PatternSyntaxException.class, () -> "A+B".split("+"));
        assertArrayEquals(new String[] {"A", "B"}, SplitFields.literalDelimiter("A+B", "+", -1));
    }

    @ParameterizedTest(name = "S12 input={0}")
    @MethodSource("boundaries")
    void S12_boundaryContentsMatchForBothLiteralForms(String input, String[] drop, String[] keep) {
        assertArrayEquals(drop, SplitFields.literalDot(input, 0));
        assertArrayEquals(keep, SplitFields.literalDot(input, -1));
        assertArrayEquals(drop, SplitFields.literalDelimiter(input, ".", 0));
        assertArrayEquals(keep, SplitFields.literalDelimiter(input, ".", -1));
    }

    static Stream<Arguments> boundaries() {
        return Stream.of(
                Arguments.of("A.B", new String[] {"A", "B"}, new String[] {"A", "B"}),
                Arguments.of("A.B.", new String[] {"A", "B"}, new String[] {"A", "B", ""}),
                Arguments.of(".A", new String[] {"", "A"}, new String[] {"", "A"}),
                Arguments.of("A..B", new String[] {"A", "", "B"}, new String[] {"A", "", "B"}),
                Arguments.of("A", new String[] {"A"}, new String[] {"A"}),
                Arguments.of("", new String[] {""}, new String[] {""}),
                Arguments.of(".", new String[0], new String[] {"", ""}),
                Arguments.of("A..", new String[] {"A"}, new String[] {"A", "", ""}),
                Arguments.of("..A", new String[] {"", "", "A"}, new String[] {"", "", "A"}),
                Arguments.of("...", new String[0], new String[] {"", "", "", ""}));
    }

    @ParameterizedTest(name = "S13 delimiter={0}")
    @MethodSource("literalDelimiters")
    void S13_variableDelimiterIsQuotedAsAWhole(String delimiter) {
        String input = "left" + delimiter + "right" + delimiter;
        assertArrayEquals(new String[] {"left", "right", ""}, SplitFields.literalDelimiter(input, delimiter, -1));
        assertArrayEquals(new String[] {"left", "right"}, SplitFields.literalDelimiter(input, delimiter, 0));
    }

    static Stream<String> literalDelimiters() {
        return Stream.of("|", "+", "(", ")", "::", "\\E");
    }
}
