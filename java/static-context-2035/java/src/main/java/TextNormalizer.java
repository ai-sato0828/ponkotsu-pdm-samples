import java.util.Locale;

public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
