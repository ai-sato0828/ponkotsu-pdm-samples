package example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SharedStateTest {
    private RequestNumberService service;

    @BeforeEach
    void setUp() {
        service = new RequestNumberService();
    }

    @Test
    void first() {
        assertEquals(1, service.issue());
    }

    @Test
    void second() {
        assertEquals(1, service.issue());
    }
}
