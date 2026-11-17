package info.syachiku.ternary;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TernarySelectionTest {
    record Case(String name, boolean useExisting, Boolean configured,
                boolean brokenThrows, Boolean expected) {
        @Override public String toString() { return name; }
    }

    static Stream<Case> cases() {
        return Stream.of(
                new Case("choose-null", true, null, true, null),
                new Case("choose-true", true, Boolean.TRUE, false, Boolean.TRUE),
                new Case("choose-false", true, Boolean.FALSE, false, Boolean.FALSE),
                new Case("ignore-null", false, null, false, Boolean.FALSE),
                new Case("ignore-true", false, Boolean.TRUE, false, Boolean.FALSE),
                new Case("ignore-false", false, Boolean.FALSE, false, Boolean.FALSE));
    }

    @ParameterizedTest(name = "six-input contract: {0}")
    @MethodSource("cases")
    void comparesFailureAndFixedValueWithoutLosingTheThirdState(Case input) {
        if (input.brokenThrows()) {
            assertThrows(NullPointerException.class,
                    () -> TernarySelection.broken(input.useExisting(), input.configured()));
        } else {
            assertEquals(input.expected(),
                    TernarySelection.broken(input.useExisting(), input.configured()));
        }
        Boolean selected = TernarySelection.preserveNullable(
                input.useExisting(), input.configured());
        assertEquals(input.expected(), selected);
        if (input.useExisting()) {
            assertSame(input.configured(), selected);
        } else {
            assertSame(Boolean.FALSE, selected);
        }
    }

    @Test
    void keepingNullDoesNotMakeALaterIfSafe() {
        Boolean selected = TernarySelection.preserveNullable(true, null);
        assertNull(selected);
        assertThrows(NullPointerException.class, () -> {
            if (selected) {
                throw new AssertionError("this body must not be reached");
            }
        });
    }

    @ParameterizedTest(name = "separate true-only boundary: {0}")
    @NullSource
    @ValueSource(booleans = {true, false})
    void collapsingNullToFalseIsASeparateDecision(Boolean configured) {
        Boolean selected = TernarySelection.preserveNullable(true, configured);
        assertSame(configured, selected);
        boolean execute = Boolean.TRUE.equals(selected);
        assertEquals(configured != null && configured.booleanValue(), execute);
    }
}
