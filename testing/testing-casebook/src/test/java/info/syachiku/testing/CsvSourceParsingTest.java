package info.syachiku.testing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class CsvSourceParsingTest {
    @ParameterizedTest(name = "{0}")
    @CsvSource(value = {
        "unquoted-null,",
        "quoted-empty,''",
        "quoted-space,' '",
        "quoted-comma,'a,b'",
        "literal-NULL,NULL"
    }, ignoreLeadingAndTrailingWhitespace = true)
    void csvInputIsWhatWeIntended(String kind, String input) {
        switch (kind) {
            case "unquoted-null" -> assertNull(input);
            case "quoted-empty" -> assertEquals("", input);
            case "quoted-space" -> assertEquals(" ", input);
            case "quoted-comma" -> assertEquals("a,b", input);
            case "literal-NULL" -> assertEquals("NULL", input);
            default -> fail("Unknown case: " + kind);
        }
    }
    @ParameterizedTest
    @CsvSource(value = {"trimmed,  a  "})
    void unquotedWhitespaceIsTrimmedByDefault(String kind, String input) {
        assertEquals("a", input);
    }

    @ParameterizedTest
    @CsvSource(value = {"preserved,  a  "}, ignoreLeadingAndTrailingWhitespace = false)
    void unquotedWhitespaceCanBePreserved(String kind, String input) {
        assertEquals("  a  ", input);
    }
}
