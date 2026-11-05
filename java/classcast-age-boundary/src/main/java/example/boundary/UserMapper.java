package example.boundary;

import java.math.BigDecimal;
import java.util.Map;

public final class UserMapper {
    public record User(int age) {}

    public enum Problem { REQUIRED, UNSUPPORTED_TYPE, NOT_EXACT_INT, AGE_OUT_OF_RANGE }

    public static final class InvalidInputException extends IllegalArgumentException {
        private final Problem problem;

        InvalidInputException(Problem problem, Throwable cause) {
            super("age " + problem, cause);
            this.problem = problem;
        }

        public Problem problem() {
            return problem;
        }
    }

    public User toUser(Map<String, ?> attributes) {
        int age = exactAge(attributes.get("age"));
        if (age < 0 || age > 130) {
            throw new InvalidInputException(Problem.AGE_OUT_OF_RANGE, null);
        }
        return new User(age);
    }

    private int exactAge(Object value) {
        if (value == null) {
            throw new InvalidInputException(Problem.REQUIRED, null);
        }
        try {
            if (value instanceof Integer number) {
                return number;
            }
            if (value instanceof Long number) {
                return Math.toIntExact(number);
            }
            if (value instanceof BigDecimal number) {
                return number.intValueExact();
            }
        } catch (ArithmeticException e) {
            throw new InvalidInputException(Problem.NOT_EXACT_INT, e);
        }
        throw new InvalidInputException(Problem.UNSUPPORTED_TYPE, null);
    }
}
