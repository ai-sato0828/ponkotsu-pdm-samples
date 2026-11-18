package example.importcode;

public final class FixedWidthDemo {
    private FixedWidthDemo() {}

    public static void main(String[] args) {
        for (String input : new String[]{"AB1234", "AB123", "", "AB1234TAIL"}) {
            String before;
            try {
                before = FixedWidthCode.extractUnchecked(input);
            } catch (IndexOutOfBoundsException failure) {
                before = failure.getClass().getSimpleName();
            }
            String after;
            try {
                after = FixedWidthCode.extractPayload(input);
            } catch (IllegalArgumentException failure) {
                after = failure.getClass().getSimpleName() + ": " + failure.getMessage();
            }
            System.out.printf("input=\"%s\" before=%s after=%s%n", input, before, after);
        }
    }
}
