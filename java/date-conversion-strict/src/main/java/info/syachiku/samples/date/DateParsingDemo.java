package info.syachiku.samples.date;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateParsingDemo {
    public static void main(String[] args) {
        String input = "2026/02/30";
        var patternDefault = DateTimeFormatter.ofPattern("uuuu/MM/dd");
        System.out.println("既定の解析方針: " + patternDefault.getResolverStyle());
        System.out.println("SMART: " + input + " -> "
                + LocalDate.parse(input, patternDefault));
        try {
            DateInputs.parseSlashDate(input);
            throw new AssertionError("存在しない日付を受け付けました");
        } catch (DateTimeParseException expected) {
            System.out.println("STRICT: " + input + " -> DateTimeParseException");
        }
        System.out.println("必須・正常: " + DateInputs.parseSlashDate("2024/02/29"));
        System.out.println("任意・未入力: " + DateInputs.parseOptionalDate(" "));
    }
}
