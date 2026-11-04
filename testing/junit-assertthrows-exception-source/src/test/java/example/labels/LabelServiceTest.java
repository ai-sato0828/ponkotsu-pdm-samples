package example.labels;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LabelServiceTest {
    private final LabelService service = new LabelService();

    @Test
    void rejectsParcelAboveTheMaximum() {
        PostalCode destination = new PostalCode("1234567");

        assertThrows(IllegalArgumentException.class,
                () -> service.createLabel(destination, 2_001));
    }

    @Test
    void createsLabelAtTheMaximum() {
        PostalCode destination = new PostalCode("1234567");

        assertEquals("1234567 / 2000g", service.createLabel(destination, 2_000));
    }

    @Test
    void rejectsZeroWeight() {
        PostalCode destination = new PostalCode("1234567");

        assertThrows(IllegalArgumentException.class,
                () -> service.createLabel(destination, 0));
    }
}
