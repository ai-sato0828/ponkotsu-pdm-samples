package info.syachiku.samples.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class ReceiptAmountDemo {
    private ReceiptAmountDemo() {}

    public static void main(String[] args) throws SQLException {
        BigDecimal input = new BigDecimal("1200.0");
        Observation result = compare(input);
        BigDecimal stored = result.storedAmount();

        System.out.printf("SQL matched 1 row: %s%n", result.receiptId());
        System.out.printf("input=%s (scale %d)%n", input.toPlainString(), input.scale());
        System.out.printf("stored=%s (scale %d)%n", stored.toPlainString(), stored.scale());
        System.out.printf("equals=%s, compareTo=%d%n", input.equals(stored), input.compareTo(stored));
        System.out.printf("HashMap raw=%s%n", result.rawMatch());
        System.out.printf("HashMap normalized=%s%n", result.normalizedMatch());
    }

    static Observation compare(BigDecimal input) throws SQLException {
        BigDecimal lookupKey = fixedTwoDecimals(input);
        try (var db = DriverManager.getConnection("jdbc:h2:mem:")) {
            try (var schema = db.createStatement()) {
                schema.execute("CREATE TABLE receipts (id VARCHAR(4) PRIMARY KEY, amount DECIMAL(12, 2) NOT NULL)");
            }

            insert(db, "R001", input);
            insert(db, "R002", new BigDecimal("1300.00"));
            Receipt receipt = findOne(db, input);

            Map<BigDecimal, String> raw = new HashMap<>();
            raw.put(receipt.amount(), receipt.id());

            Map<BigDecimal, String> normalized = new HashMap<>();
            normalized.put(fixedTwoDecimals(receipt.amount()), receipt.id());
            return new Observation(receipt.id(), receipt.amount(), raw.get(input), normalized.get(lookupKey));
        }
    }

    private static BigDecimal fixedTwoDecimals(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    private static void insert(Connection db, String id, BigDecimal amount) throws SQLException {
        try (var insert = db.prepareStatement("INSERT INTO receipts (id, amount) VALUES (?, ?)")) {
            insert.setString(1, id);
            insert.setBigDecimal(2, amount);
            insert.executeUpdate();
        }
    }

    private static Receipt findOne(Connection db, BigDecimal amount) throws SQLException {
        try (var find = db.prepareStatement("SELECT id, amount FROM receipts WHERE amount = ?")) {
            find.setBigDecimal(1, amount);
            try (var rows = find.executeQuery()) {
                if (!rows.next()) {
                    throw new IllegalStateException("Expected one matching test row");
                }
                Receipt receipt = new Receipt(rows.getString("id"), rows.getBigDecimal("amount"));
                if (rows.next()) {
                    throw new IllegalStateException("Expected one matching test row");
                }
                return receipt;
            }
        }
    }

    private record Receipt(String id, BigDecimal amount) {}

    record Observation(String receiptId, BigDecimal storedAmount, String rawMatch, String normalizedMatch) {}
}
