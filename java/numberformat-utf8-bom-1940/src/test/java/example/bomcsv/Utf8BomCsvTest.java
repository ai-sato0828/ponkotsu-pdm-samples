package example.bomcsv;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Utf8BomCsvTest {
    @Test
    void rawUtf8DecodingLeavesTheFirstBomCharacterAndOnlyFirstNumberFails() throws Exception {
        byte[] input = BomCsvDemo.withBom();
        assertArrayEquals(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, Arrays.copyOf(input, 3));
        assertArrayEquals(BomCsvDemo.withoutBom(), Arrays.copyOfRange(input, 3, input.length));
        var rows = Utf8BomCsv.parseRaw(input);
        assertEquals(2, rows.size());
        String first = rows.get(0).numberText();
        assertEquals("\uFEFF123", first);
        assertEquals(0xFEFF, first.codePointAt(0));
        assertEquals(first, first.trim());
        assertEquals(first, first.strip());
        assertThrowsExactly(NumberFormatException.class, () -> Integer.parseInt(first));
        assertThrowsExactly(NumberFormatException.class, () -> Integer.parseInt(first.trim()));
        assertThrowsExactly(NumberFormatException.class, () -> Integer.parseInt(first.strip()));
        assertEquals(456, Integer.parseInt(rows.get(1).numberText()));
        assertEquals(List.of("SKU-001", "SKU-002"), rows.stream().map(Utf8BomCsv.Row::sku).toList());
    }

    @Test
    void streamBoundaryRemovesOnlyTheInitialUtf8BomBeforeCsvParsing() throws Exception {
        var rows = Utf8BomCsv.parseWithUtf8BomBoundary(BomCsvDemo.withBom());
        assertEquals(List.of(123, 456), rows.stream().map(row -> Integer.parseInt(row.numberText())).toList());
        assertEquals(List.of("SKU-001", "SKU-002"), rows.stream().map(Utf8BomCsv.Row::sku).toList());
    }

    @Test
    void theSameHeaderlessCsvWithoutBomNeedsNoSpecialDataCleanup() throws Exception {
        byte[] input = BomCsvDemo.withoutBom();
        assertEquals("123,SKU-001\n456,SKU-002\n", new String(input, StandardCharsets.UTF_8));
        var raw = Utf8BomCsv.parseRaw(input);
        var fixed = Utf8BomCsv.parseWithUtf8BomBoundary(input);
        assertEquals(raw, fixed);
        assertEquals(List.of(123, 456), fixed.stream().map(row -> Integer.parseInt(row.numberText())).toList());
    }

    @Test
    void embeddedFeffIsNotAStreamBomAndIsNotSilentlyRemoved() throws Exception {
        var rows = Utf8BomCsv.parseWithUtf8BomBoundary(BomCsvDemo.embeddedBom());
        assertEquals(2, rows.size());
        assertEquals(123, Integer.parseInt(rows.get(0).numberText()));
        assertEquals("\uFEFF456", rows.get(1).numberText());
        assertThrowsExactly(NumberFormatException.class, () -> Integer.parseInt(rows.get(1).numberText()));
        assertEquals("SKU-002", rows.get(1).sku());
    }
}
