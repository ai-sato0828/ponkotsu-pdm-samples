package example.displayname;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisplayNameValidatorTest {
    @Test
    void oldValidatorRejectsJapaneseNameAtTheLimit() {
        assertFalse(DisplayNameDemo.acceptsBefore("𠮷田葵"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"葵", " 葵 ", "田中葵", "𠮷田葵", "A😀B"})
    void acceptsUpToThreeCodePoints(String name) {
        assertTrue(DisplayNameValidator.accepts(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"  葵 ", "𠮷田葵子", "😀😀😀😀", "A👩‍💻"})
    void rejectsMoreThanThreeCodePoints(String name) {
        assertFalse(DisplayNameValidator.accepts(name));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "　"})
    void rejectsMissingAndBlankNames(String name) {
        assertFalse(DisplayNameValidator.accepts(name));
    }
}
