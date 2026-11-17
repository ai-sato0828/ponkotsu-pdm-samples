package info.syachiku.samples.stringutils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BlankSelectionTest {
    private record Input(String label, String value, boolean empty, boolean blank,
                         boolean missingName, String memo) {
        @Override
        public String toString() {
            return label;
        }
    }

    static Stream<Input> cases() {
        return Stream.of(
                new Input("NULL", null, true, true, true, "(未設定)"),
                new Input("EMPTY", "", true, true, true, "(未設定)"),
                new Input("HALF_SPACE", " ", false, true, true, " "),
                new Input("FULL_SPACE", "\u3000", false, true, true, "\u3000"),
                new Input("NBSP", "\u00A0", false, false, false, "\u00A0"),
                new Input("TAB_LF", "\t\n", false, true, true, "\t\n"),
                new Input("TEXT", "Alice", false, false, false, "Alice"));
    }

    @ParameterizedTest(name = "Apache: {0}")
    @MethodSource("cases")
    void apachePredicatesFollowEmptyAndBlankRules(Input input) {
        assertEquals(input.empty(), StringUtils.isEmpty(input.value()));
        assertEquals(input.blank(), StringUtils.isBlank(input.value()));
    }

    @ParameterizedTest(name = "String: {0}")
    @MethodSource("cases")
    void jdkPredicatesRejectNullAndClassifyOtherInputs(Input input) {
        if (input.value() == null) {
            assertThrows(NullPointerException.class, () -> input.value().isEmpty());
            assertThrows(NullPointerException.class, () -> input.value().isBlank());
        } else {
            assertEquals(input.empty(), input.value().isEmpty());
            assertEquals(input.blank(), input.value().isBlank());
        }
    }

    @ParameterizedTest(name = "Name requirement: {0}")
    @MethodSource("cases")
    void displayNameMissingFollowsDeclaredBlankRequirement(Input input) {
        assertEquals(input.missingName(), InputPolicy.isDisplayNameMissing(input.value()));
    }

    @ParameterizedTest(name = "Memo requirement: {0}")
    @MethodSource("cases")
    void optionalMemoDefaultsOnlyForNullOrZeroLength(Input input) {
        assertEquals(input.memo(), InputPolicy.memoOrDefault(input.value()));
    }

    @ParameterizedTest(name = "JDK-only alternative: {0}")
    @MethodSource("cases")
    void jdkOnlyAlternativeMeetsTheSameRequirements(Input input) {
        assertEquals(input.missingName(), JdkOnlyInputPolicy.isDisplayNameMissing(input.value()));
        assertEquals(input.memo(), JdkOnlyInputPolicy.memoOrDefault(input.value()));
    }

    @ParameterizedTest(name = "Spring: {0}")
    @MethodSource("cases")
    void springUsesPositiveTextAndLengthPredicates(Input input) {
        assertEquals(!input.blank(), org.springframework.util.StringUtils.hasText(input.value()));
        assertEquals(!input.empty(), org.springframework.util.StringUtils.hasLength(input.value()));
    }

    @Test
    void springStringUtilsInThePinnedVersionHasNoIsBlankMethod() {
        assertFalse(Arrays.stream(org.springframework.util.StringUtils.class.getMethods())
                .anyMatch(method -> method.getName().equals("isBlank")));
    }
}
