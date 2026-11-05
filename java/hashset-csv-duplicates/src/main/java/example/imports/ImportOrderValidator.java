package example.imports;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ImportOrderValidator {
    private ImportOrderValidator() {}

    public record CsvRow(int lineNumber, String orderNo) {}

    public record Result(List<Integer> missingRows, List<Integer> duplicateRows) {}

    public static Result inspect(List<CsvRow> rows) {
        Set<String> seenOrderNos = new HashSet<>();
        List<Integer> missingRows = new ArrayList<>();
        List<Integer> duplicateRows = new ArrayList<>();

        for (CsvRow row : rows) {
            String orderNo = row.orderNo();
            if (orderNo == null || orderNo.isBlank()) {
                missingRows.add(row.lineNumber());
                continue;
            }
            if (!seenOrderNos.add(orderNo)) {
                duplicateRows.add(row.lineNumber());
            }
        }
        return new Result(List.copyOf(missingRows), List.copyOf(duplicateRows));
    }
}
