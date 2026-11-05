public class BlankStringDemo {
    static boolean isMissing(String value) {
        return value == null || value.isBlank();
    }

    public static void main(String[] args) {
        System.out.println("input\tisEmpty\tisBlank\tisMissing");
        printResult("null", null);
        printResult("empty", "");
        printResult("U+0020", " ");
        printResult("U+3000", "\u3000");
        printResult("TAB", "\t");
        printResult("LF", "\n");
        printResult("letters", " A ");
        printResult("U+00A0", "\u00A0");
    }

    private static void printResult(String label, String value) {
        String empty = value == null ? "NPE" : Boolean.toString(value.isEmpty());
        String blank = value == null ? "NPE" : Boolean.toString(value.isBlank());
        System.out.printf("%s\t%s\t%s\t%b%n", label, empty, blank, isMissing(value));
    }
}
