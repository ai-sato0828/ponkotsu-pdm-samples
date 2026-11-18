import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerRegistrationTest {
    private final List<CustomerRegistration.Customer> saved = new ArrayList<>();
    private final CustomerRegistration service = new CustomerRegistration(saved::add);

    @Test
    void missingExternalIdRejectsWithoutSaving() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, "佐藤"));
        assertEquals(List.of(), saved);
    }

    @Test
    void missingDisplayNameIsAllowedAndSavedOnce() {
        var actual = service.register("EXT-01", null);

        assertEquals(Optional.empty(), actual.displayName());
        assertEquals(List.of(actual), saved);
    }

    @Test
    void providedDisplayNameIsPreserved() {
        var actual = service.register("EXT-01", "佐藤");

        assertEquals(Optional.of("佐藤"), actual.displayName());
        assertEquals(List.of(actual), saved);
    }
}
