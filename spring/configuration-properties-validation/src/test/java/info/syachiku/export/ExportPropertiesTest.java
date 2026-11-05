package info.syachiku.export;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.validation.FieldError;

import static org.assertj.core.api.Assertions.assertThat;

class ExportPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(ExportApplication.class);

    @ParameterizedTest
    @ValueSource(ints = {1, 500})
    void acceptsBothBoundaries(int size) {
        contextRunner.withPropertyValues("export.batch-size=" + size).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(ExportProperties.class).batchSize()).isEqualTo(size);
        });
    }

    @Test
    void missingSizeFailsTheRequiredConstraint() {
        contextRunner.run(context ->
                assertBatchSizeViolation(context.getStartupFailure(), "NotNull"));
    }

    @ParameterizedTest
    @CsvSource({"0, Min", "501, Max"})
    void outOfRangeSizeFailsItsConstraint(String value, String constraint) {
        contextRunner.withPropertyValues("export.batch-size=" + value).run(context ->
                assertBatchSizeViolation(context.getStartupFailure(), constraint));
    }

    @Test
    void nonNumericValueFailsDuringConversion() {
        contextRunner.withPropertyValues("export.batch-size=abc").run(context -> {
            Throwable failure = context.getStartupFailure();
            assertThat(failure).isNotNull();
            assertThat(findCause(failure, ConversionFailedException.class)).isNotNull();
            assertThat(findCause(failure, BindValidationException.class)).isNull();
        });
    }

    private static void assertBatchSizeViolation(Throwable failure, String constraint) {
        assertThat(failure).as("Spring context startup failure").isNotNull();
        var violation = findCause(failure, BindValidationException.class);
        assertThat(violation).isNotNull();
        assertThat(violation.getValidationErrors().getAllErrors()).anySatisfy(error -> {
            assertThat(error).isInstanceOf(FieldError.class);
            assertThat(((FieldError) error).getField()).isEqualTo("batchSize");
            assertThat(error.getCode()).isEqualTo(constraint);
        });
    }

    private static <T extends Throwable> T findCause(Throwable failure, Class<T> type) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (type.isInstance(cause)) {
                return type.cast(cause);
            }
        }
        return null;
    }
}
