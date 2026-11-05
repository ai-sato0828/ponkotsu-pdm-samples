import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class PreviewVerification {
    static List<Account> previewAccounts(List<String[]> rows, AccountMapper mapper) {
        return rows.stream().map(mapper::toAccount).toList();
    }

    public static void main(String[] args) {
        var mapper = new AccountMapper(100);
        Function<List<String[]>, List<Account>> preview = rows -> previewAccounts(rows, mapper);
        var importer = new ClassImporter(mapper);
        var saves = new ArrayList<List<Account>>();

        var validRows = List.<String[]>of(row("100"));
        var shown = preview.apply(validRows);
        var expected = List.of(new Account("a@example.test", 100));
        require(shown.equals(expected) && saves.isEmpty(), "100: preview and zero saves");
        importer.importAccounts(validRows, batch -> saves.add(List.copyOf(batch)));
        require(saves.equals(List.of(shown)), "100: save previewed values exactly once");
        System.out.println("100: preview accepts, preview saves=0, import saves=1");

        for (var invalidRows : List.of(List.<String[]>of(row("101")), List.of(row("100"), row("101")))) {
            saves.clear();
            String previewError = rejected(() -> preview.apply(invalidRows));
            require(saves.isEmpty(), "invalid preview must not save");
            String importError = rejected(() -> importer.importAccounts(invalidRows,
                    batch -> saves.add(List.copyOf(batch))));
            require("契約数は100以下".equals(previewError) && previewError.equals(importError),
                    "preview and import must reject with the same rule");
            require(saves.isEmpty(), "invalid import must not save");
            System.out.printf("%s: both reject, preview saves=0, import saves=0%n",
                    invalidRows.size() == 1 ? "101" : "100 then 101");
        }
    }

    private static String[] row(String seats) {
        return new String[]{"a@example.test", seats};
    }

    private static String rejected(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        throw new AssertionError("expected rejection");
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }
}
