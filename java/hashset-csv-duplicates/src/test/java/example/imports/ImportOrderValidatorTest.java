package example.imports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import example.imports.ImportOrderValidator.CsvRow;
import example.imports.ImportOrderValidator.Result;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ImportOrderValidatorTest {
    private record EmployeeKey(String companyCode, String employeeNo) {}

    @Test
    void addsAnEmployeeKeyOnlyOnce() {
        Set<EmployeeKey> keys = new HashSet<>();
        EmployeeKey original = new EmployeeKey("C01", "E001");
        assertTrue(keys.add(original));
        assertFalse(keys.add(new EmployeeKey("C01", "E001")));
        assertEquals(Set.of(original), keys);
    }

    @Test
    void reportsEveryMissingNumberWithoutMarkingDuplicates() {
        List<CsvRow> rows = List.of(
                new CsvRow(2, null), new CsvRow(3, null),
                new CsvRow(4, ""), new CsvRow(5, ""),
                new CsvRow(6, " "), new CsvRow(7, " "));

        assertEquals(new Result(List.of(2, 3, 4, 5, 6, 7), List.of()),
                ImportOrderValidator.inspect(rows));
    }

    @Test
    void reportsDuplicateAtOriginalStartingLine() {
        List<CsvRow> rows = List.of(
                new CsvRow(2, "A01"),
                new CsvRow(3, "B02"), // 別の列の改行で、このレコードは3〜4行目
                new CsvRow(5, "A01"));

        assertEquals(new Result(List.of(), List.of(5)),
                ImportOrderValidator.inspect(rows));
    }
}
