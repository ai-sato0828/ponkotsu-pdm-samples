package info.syachiku.testing;

public final class TitleRule {
    private TitleRule() {}

    public static boolean accepts(String title) {
        if (title == null || title.isBlank()) return false;
        int length = title.codePointCount(0, title.length());
        return length >= 1 && length <= 100;
    }
}
