package example.labels;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MisleadingLabelTest {
    @Test
    void showsWhichExceptionMakesTheMisleadingAssertionPass() {
        LabelService service = new LabelService();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PostalCode destination = new PostalCode("12-345");
            service.createLabel(destination, 2_001);
        });

        assertEquals("郵便番号は半角数字7桁です", thrown.getMessage());
    }
}
