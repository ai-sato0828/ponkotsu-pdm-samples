package example.displayname;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class DisplayNameDemo {
    private static final Pattern GRAPHEME = Pattern.compile("\\X");

    private DisplayNameDemo() {
    }

    static boolean acceptsBefore(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return name.length() <= 3;
    }

    public static void main(String[] args) {
        String name = "𠮷田葵";
        System.out.println("name=" + name);
        System.out.println("before=" + acceptsBefore(name));
        System.out.println("after=" + DisplayNameValidator.accepts(name));

        System.out.println("text | UTF-16 | code points | graphemes | UTF-8 bytes");
        for (String text : new String[] {name, "😀", "e\u0301", "👩‍💻"}) {
            System.out.printf("%s | %d | %d | %d | %d%n",
                    text, text.length(), text.codePointCount(0, text.length()),
                    GRAPHEME.matcher(text).results().count(),
                    text.getBytes(StandardCharsets.UTF_8).length);
        }
    }
}
