package example.boundary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import example.boundary.UserMapper.InvalidInputException;
import example.boundary.UserMapper.Problem;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class UserMapperTest {
    private final UserMapper mapper = new UserMapper();

    static Stream<Arguments> validAges() {
        return Stream.of(Arguments.of(20, 20), Arguments.of(20L, 20),
                Arguments.of(new BigDecimal("20.0"), 20),
                Arguments.of(0, 0), Arguments.of(130, 130));
    }

    @ParameterizedTest
    @MethodSource("validAges")
    void acceptsExactAge(Object input, int expected) {
        assertEquals(expected, mapper.toUser(Map.of("age", input)).age());
    }

    @Test
    void missingKeyIsRequired() {
        assertProblem(Problem.REQUIRED, Map.of());
    }

    @Test
    void explicitNullIsRequired() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("age", null);
        assertProblem(Problem.REQUIRED, attributes);
    }

    static Stream<Object> unsupportedTypes() {
        return Stream.of("20", 20.0);
    }

    @ParameterizedTest
    @MethodSource("unsupportedTypes")
    void rejectsUnsupportedType(Object input) {
        assertProblem(Problem.UNSUPPORTED_TYPE, Map.of("age", input));
    }

    static Stream<Number> inexactInputs() {
        return Stream.of(new BigDecimal("20.5"), 4_294_967_316L);
    }

    @ParameterizedTest
    @MethodSource("inexactInputs")
    void rejectsValuesIntValueWouldTurnIntoTwenty(Number input) {
        assertEquals(20, input.intValue());
        assertProblem(Problem.NOT_EXACT_INT, Map.of("age", input));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 131})
    void rejectsAgeOutsideBusinessRange(int input) {
        assertProblem(Problem.AGE_OUT_OF_RANGE, Map.of("age", input));
    }

    private void assertProblem(Problem expected, Map<String, ?> input) {
        InvalidInputException error = assertThrows(InvalidInputException.class,
                () -> mapper.toUser(input));
        assertEquals(expected, error.problem());
    }
}
