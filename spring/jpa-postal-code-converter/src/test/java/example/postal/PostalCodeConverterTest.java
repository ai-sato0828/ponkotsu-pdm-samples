package example.postal;

import example.postal.fixed.PostalCodeConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class PostalCodeConverterTest {
    private final PostalCodeConverter converter = new PostalCodeConverter();

    @Test
    void stringConversionPreservesTheLeadingZeroInBothDirections() {
        PostalCode original = new PostalCode("0123456");
        assertEquals("0123456", converter.convertToDatabaseColumn(original));
        assertEquals(original, converter.convertToEntityAttribute("0123456"));
    }

    @Test
    void optionalFieldNullMapsToSqlNullAndBack() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "12345678", "12A4567", "１２３４５６７", "123-4567"})
    void invalidDomainInputIsRejectedInsteadOfNormalized(String input) {
        assertThrows(IllegalArgumentException.class, () -> new PostalCode(input));
    }

    @Test
    void aValueObjectCannotContainNull() {
        assertThrows(NullPointerException.class, () -> new PostalCode(null));
    }
}
