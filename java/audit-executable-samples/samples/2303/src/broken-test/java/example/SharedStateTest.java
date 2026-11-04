package example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SharedStateTest {
    private static final RequestNumberService service = new RequestNumberService();

    @Test
    void first() {
        assertEquals(1, service.issue());
    }

    @Test
    void second() {
        assertEquals(1, service.issue());
    }
}
