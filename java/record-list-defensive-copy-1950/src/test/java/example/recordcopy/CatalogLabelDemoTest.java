package example.recordcopy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CatalogLabelDemoTest {
    @Test
    void recordWithoutCopySharesInputAndAccessorChanges() {
        List<String> input = new ArrayList<>(List.of("java"));
        var label = new CatalogLabelDemo.SharedLabel("SKU-001", input);
        input.add("sale");
        assertSame(input, label.tags());
        assertEquals(List.of("java", "sale"), label.tags());
        label.tags().add("preview");
        assertEquals(List.of("java", "sale", "preview"), input);
        assertEquals("SKU-001", label.sku());
    }

    @Test
    void compactConstructorKeepsTheTagsAtCreation() {
        List<String> input = new ArrayList<>(List.of("java"));
        var label = new CatalogLabelDemo.SnapshotLabel("SKU-001", input);
        input.add("sale");
        assertEquals(List.of("java", "sale"), input);
        assertEquals(List.of("java"), label.tags());
        assertEquals("SKU-001", label.sku());
    }

    @Test
    void snapshotAccessorRejectsAddAndKeepsTheTags() {
        var label = new CatalogLabelDemo.SnapshotLabel("SKU-001",
                new ArrayList<>(List.of("java")));
        assertThrowsExactly(UnsupportedOperationException.class,
                () -> label.tags().add("preview"));
        assertEquals(List.of("java"), label.tags());
    }

    @Test
    void nullElementIsAcceptedByTheSharedVersionButRejectedByCopyOf() {
        List<String> input = Arrays.asList("java", null);
        var shared = new CatalogLabelDemo.SharedLabel("SKU-001", input);
        assertEquals("java", shared.tags().get(0));
        assertNull(shared.tags().get(1));
        assertThrowsExactly(NullPointerException.class,
                () -> new CatalogLabelDemo.SnapshotLabel("SKU-001", input));
    }
}
