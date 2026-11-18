package example.bomcsv;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

public class BomCsvDemo {
    public static void main(String[] args) throws Exception {
        byte[] input = withBom();
        System.out.println("INPUT prefix=" + HexFormat.ofDelimiter(" ").withUpperCase().formatHex(input, 0, 6));
        List<Utf8BomCsv.Row> raw = Utf8BomCsv.parseRaw(input);
        String first = raw.get(0).numberText();
        System.out.printf("RAW row=1 codePoints=%s trimSame=%s stripSame=%s number=%s%n",
                codePoints(first), first.equals(first.trim()), first.equals(first.strip()), number(first));
        System.out.printf("RAW row=2 codePoints=%s number=%s%n",
                codePoints(raw.get(1).numberText()), number(raw.get(1).numberText()));
        System.out.println("FIXED numbers=" + numbers(Utf8BomCsv.parseWithUtf8BomBoundary(input)));
        System.out.println("NO_BOM numbers=" + numbers(Utf8BomCsv.parseWithUtf8BomBoundary(withoutBom())));
        String embedded = Utf8BomCsv.parseWithUtf8BomBoundary(embeddedBom()).get(1).numberText();
        System.out.printf("EMBEDDED row=2 codePoints=%s number=%s%n", codePoints(embedded), number(embedded));
    }

    static byte[] withoutBom() {
        return "123,SKU-001\n456,SKU-002\n".getBytes(StandardCharsets.UTF_8);
    }

    static byte[] withBom() {
        byte[] text = withoutBom();
        byte[] bytes = new byte[text.length + 3];
        bytes[0] = (byte) 0xEF;
        bytes[1] = (byte) 0xBB;
        bytes[2] = (byte) 0xBF;
        System.arraycopy(text, 0, bytes, 3, text.length);
        return bytes;
    }

    static byte[] embeddedBom() {
        return "123,SKU-001\n\uFEFF456,SKU-002\n".getBytes(StandardCharsets.UTF_8);
    }

    private static List<Integer> numbers(List<Utf8BomCsv.Row> rows) {
        return rows.stream().map(row -> Integer.parseInt(row.numberText())).toList();
    }

    private static String codePoints(String text) {
        return text.codePoints().mapToObj(value -> "U+%04X".formatted(value))
                .collect(Collectors.joining(","));
    }

    private static String number(String text) {
        try {
            return Integer.toString(Integer.parseInt(text));
        } catch (NumberFormatException failure) {
            return failure.getClass().getSimpleName();
        }
    }
}
