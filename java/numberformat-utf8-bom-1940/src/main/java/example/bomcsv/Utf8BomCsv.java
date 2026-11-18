package example.bomcsv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

public final class Utf8BomCsv {
    private Utf8BomCsv() {}

    public static List<Row> parseRaw(byte[] bytes) throws IOException {
        return parse(new ByteArrayInputStream(bytes));
    }

    public static List<Row> parseWithUtf8BomBoundary(byte[] bytes) throws IOException {
        BOMInputStream input = BOMInputStream.builder()
                .setInputStream(new ByteArrayInputStream(bytes))
                .setByteOrderMarks(ByteOrderMark.UTF_8)
                .setInclude(false)
                .get();
        return parse(input);
    }

    private static List<Row> parse(InputStream input) throws IOException {
        try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
            List<Row> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                rows.add(new Row(record.get(0), record.get(1)));
            }
            return List.copyOf(rows);
        }
    }

    public record Row(String numberText, String sku) {}
}
