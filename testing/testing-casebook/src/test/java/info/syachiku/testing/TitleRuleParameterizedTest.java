package info.syachiku.testing;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

class TitleRuleParameterizedTest {
    @ParameterizedTest(name = "{0}: length={1}, accepted={2}")
    @CsvSource({
        "T02,0,false", "T03,1,true", "T04,2,true",
        "T05,99,true", "T06,100,true", "T07,101,false"
    })
    void boundary(String caseId, int length, boolean accepted) {
        assertEquals(accepted, TitleRule.accepts("a".repeat(length)), caseId);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void missing(String input) { assertFalse(TitleRule.accepts(input)); }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\u3000"})
    void blank(String input) { assertFalse(TitleRule.accepts(input)); }

    static Stream<Arguments> supplementaryAndWhitespace() {
        return Stream.of(
            Arguments.of("T10", "😀".repeat(100), true),
            Arguments.of("T11", "😀".repeat(101), false),
            Arguments.of("T12", "a".repeat(99) + " ", true),
            Arguments.of("T13", "a".repeat(100) + " ", false)
        );
    }

    @ParameterizedTest(name = "{0}: accepted={2}")
    @MethodSource("supplementaryAndWhitespace")
    void unicodeAndNoTrim(String caseId, String input, boolean accepted) {
        assertEquals(accepted, TitleRule.accepts(input), caseId);
    }
}
