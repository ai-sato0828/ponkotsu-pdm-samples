package info.syachiku.shipping;

import java.util.HashSet;
import java.util.List;

import info.syachiku.shipping.ShipmentCsv.ShipmentLine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShipmentCsvTest {
    private static final String REPEATED = "SKU-100,1";
    private static final String OTHER = "SKU-200,1";
    private static final List<String> EXPECTED = List.of(REPEATED, REPEATED, OTHER);
    private static final List<String> REORDERED = List.of(OTHER, REPEATED, REPEATED);
    private static final List<String> MISSING = List.of(OTHER, REPEATED);
    private static final List<ShipmentLine> SHIPMENT = List.of(
            new ShipmentLine("SKU-200", 1),
            new ShipmentLine("SKU-100", 1),
            new ShipmentLine("SKU-100", 1));

    @Test
    void acceptsReorderedExportWithEveryLine() {
        var actual = ShipmentCsv.dataRows(SHIPMENT);

        assertThat(actual).containsExactlyInAnyOrder(REPEATED, REPEATED, OTHER);
    }

    @Test
    void orderedAssertionsDistinguishSequenceWhenItIsRequired() {
        assertThrows(AssertionError.class, () -> assertEquals(EXPECTED, REORDERED));
        assertThrows(AssertionError.class,
                () -> assertThat(REORDERED).containsExactly(REPEATED, REPEATED, OTHER));

        assertEquals(EXPECTED, List.of(REPEATED, REPEATED, OTHER));
        assertThat(EXPECTED).containsExactly(REPEATED, REPEATED, OTHER);
    }

    @Test
    void oldAssertionsAcceptTheBrokenExport() {
        var actual = ShipmentCsv.dataRows(SHIPMENT).stream().distinct().toList();
        assertEquals(MISSING, actual);

        assertEquals(new HashSet<>(EXPECTED), new HashSet<>(actual));
        assertThat(actual).containsOnly(REPEATED, REPEATED, OTHER);
    }

    @Test
    void rejectsMissingDuplicate() {
        assertThrows(AssertionError.class,
                () -> assertThat(MISSING).containsExactlyInAnyOrder(REPEATED, REPEATED, OTHER));
    }

    @Test
    void rejectsExtraDuplicate() {
        var actual = List.of(OTHER, REPEATED, REPEATED, REPEATED);

        assertThrows(AssertionError.class,
                () -> assertThat(actual).containsExactlyInAnyOrder(REPEATED, REPEATED, OTHER));
    }

    @Test
    void rejectsDifferentCountsEvenWhenTotalSizeMatches() {
        var actual = List.of(REPEATED, OTHER, OTHER);
        assertEquals(EXPECTED.size(), actual.size());

        assertThrows(AssertionError.class,
                () -> assertThat(actual).containsExactlyInAnyOrder(REPEATED, REPEATED, OTHER));
    }
}
