import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class ResourceScenariosTest {
    static Stream<Arguments> cases() {
        List<String> both = List.of("open:A", "open:B", "body", "close:B", "close:A");
        List<String> partial = List.of("open:A", "open:B", "close:A");
        return Stream.of(
                Arguments.of(ResourceScenarios.Scenario.NORMAL, both, "none", List.of()),
                Arguments.of(ResourceScenarios.Scenario.BODY_FAIL, both, "body", List.of()),
                Arguments.of(ResourceScenarios.Scenario.BODY_CLOSE_FAIL, both, "body",
                        List.of("close:B", "close:A")),
                Arguments.of(ResourceScenarios.Scenario.CLOSE_FAIL, both, "close:B",
                        List.of("close:A")),
                Arguments.of(ResourceScenarios.Scenario.ACQUIRE_CLOSE_FAIL, partial, "open:B",
                        List.of("close:A")),
                Arguments.of(ResourceScenarios.Scenario.ACQUIRE_FAIL, partial, "open:B", List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("cases")
    void syntaxDeterminesPrimarySuppressedAndCloseOrder(
            ResourceScenarios.Scenario scenario, List<String> expectedTrace,
            String expectedPrimary, List<String> expectedSuppressed) {
        ResourceScenarios.Outcome outcome = ResourceScenarios.run(scenario);
        assertEquals(expectedTrace, outcome.trace());
        assertEquals(expectedPrimary, outcome.primaryMessage());
        assertEquals(expectedSuppressed, outcome.suppressedMessages());
        if (scenario == ResourceScenarios.Scenario.NORMAL) {
            assertNull(outcome.failure());
        } else {
            assertInstanceOf(IOException.class, outcome.failure());
            assertEquals(expectedSuppressed.size(), outcome.failure().getSuppressed().length);
        }
    }
}
