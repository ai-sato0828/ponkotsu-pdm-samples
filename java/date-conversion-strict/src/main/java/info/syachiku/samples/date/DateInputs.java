package info.syachiku.samples.date;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Optional;
import java.util.regex.Pattern;

public final class DateInputs {
    private static final Pattern SLASH_SHAPE =
            Pattern.compile("[0-9]{4}/[0-9]{2}/[0-9]{2}");
    private static final DateTimeFormatter SLASH_DATE =
            DateTimeFormatter.ofPattern("uuuu/MM/dd")
                    .withResolverStyle(ResolverStyle.STRICT);

    private DateInputs() {}

    public static LocalDate parseSlashDate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("日付は必須です");
        }
        if (!SLASH_SHAPE.matcher(value).matches()) {
            throw new DateTimeParseException(
                    "日付は半角数字のuuuu/MM/ddで入力してください", value, 0);
        }
        return LocalDate.parse(value, SLASH_DATE);
    }

    public static Optional<LocalDate> parseOptionalDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(parseSlashDate(value));
    }
}
