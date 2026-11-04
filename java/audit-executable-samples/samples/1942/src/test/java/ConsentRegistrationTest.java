import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsentRegistrationTest {
    private final ConsentRegistration service = new ConsentRegistration();

    @ParameterizedTest
    @CsvSource({
            ", ASK_AGAIN, ''",
            "false, REGISTER_ONLY, save",
            "true, REGISTER_AND_SUBSCRIBE, save/subscribe"
    })
    void keepsThreeConsentOutcomes(Boolean agreed, ConsentRegistration.Outcome expected, String expectedCalls) {
        List<String> calls = new ArrayList<>();

        var actual = service.register(agreed, () -> calls.add("save"), () -> calls.add("subscribe"));

        assertEquals(expected, actual);
        assertEquals(expectedCalls, String.join("/", calls));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {false})
    void doesNotExecuteFeatureWithoutTrue(Boolean enabled) {
        assertFalse(service.shouldExecute(enabled));
    }

    @Test
    void executesFeatureWhenTrue() {
        assertTrue(service.shouldExecute(Boolean.TRUE));
    }
}
