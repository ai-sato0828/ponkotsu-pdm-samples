import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.HashMap;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BooleanMapReaderTest {
    @ParameterizedTest(name = "{0}")
    @MethodSource("states")
    void missingAndStoredNullAreDifferentBeforeUnboxing(String label, boolean present,
                                                       Boolean configured, boolean expected) {
        HashMap<String, Boolean> flags = new HashMap<>();
        if (present) {
            flags.put("enabled", configured);
        }
        assertEquals(present, flags.containsKey("enabled"));
        String before;
        if (present && configured == null) {
            // getOrDefault自体はnullを返す。booleanへ代入する段階でNPEになる。
            assertNull(flags.getOrDefault("enabled", Boolean.FALSE));
            assertThrows(NullPointerException.class,
                    () -> BooleanMapReader.readWithDefault(flags, "enabled"));
            before = "NullPointerException";
        } else {
            boolean actual = BooleanMapReader.readWithDefault(flags, "enabled");
            assertEquals(expected, actual);
            before = Boolean.toString(actual);
        }
        boolean fixed = BooleanMapReader.readTrueOnly(flags, "enabled");
        assertEquals(expected, fixed);
        System.out.printf("MAP case=%s containsKey=%s get=%s before=%s fixed=%s%n",
                label, flags.containsKey("enabled"), flags.get("enabled"), before, fixed);
    }

    static Stream<Arguments> states() {
        return Stream.of(
                arguments("absent", false, null, false),
                arguments("stored-null", true, null, false),
                arguments("stored-false", true, false, false),
                arguments("stored-true", true, true, true));
    }
}
