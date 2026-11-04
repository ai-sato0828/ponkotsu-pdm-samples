import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.DuplicateHeaderMode;

public final class CustomerCsvReader {
    public record Customer(String id, String name, String note) {}

    public static List<Customer> read(Path path) throws IOException {
        var format = CSVFormat.RFC4180.builder()
                .setHeader() // 先頭レコードからヘッダー名を読む
                .setSkipHeaderRecord(true)
                .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW)
                .get();
        List<Customer> customers = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {
            if (!parser.getHeaderNames().equals(List.of("id", "name", "note"))) {
                throw new IllegalArgumentException("CSVの列名または順番が違います");
            }
            for (CSVRecord row : parser) {
                if (row.size() != 3) {
                    throw new IllegalArgumentException(
                            "レコード" + row.getRecordNumber() + "の列数が違います");
                }
                String id = row.get("id");
                String name = row.get("name");
                if (id.isBlank() || name.isBlank()) {
                    throw new IllegalArgumentException(
                            "レコード" + row.getRecordNumber() + "の必須項目が空です");
                }
                customers.add(new Customer(id, name, row.get("note")));
            }
        }
        return customers;
    }
}
