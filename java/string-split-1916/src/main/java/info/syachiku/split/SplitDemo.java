package info.syachiku.split;

public final class SplitDemo {

    private SplitDemo() {
    }

    public static void main(String[] args) {
        System.out.println("matches(\"...\"): abc=" + "abc".matches("...") + ", a.b=" + "a.b".matches("..."));
        String input = "a.b";
        System.out.println("wrong dot: " + SplitFields.describe(SplitFields.wrongDot(input, 0)));
        System.out.println("escaped dot: " + SplitFields.describe(SplitFields.literalDot(input, 0)));
        System.out.println("quoted dot: " + SplitFields.describe(SplitFields.literalDelimiter(input, ".", 0)));
        for (String value : new String[] {"A.B", "A.B.", ".A", "A..B", "A", "", "."}) {
            System.out.println("input=\"" + value + "\" limit=0 "
                    + SplitFields.describe(SplitFields.literalDot(value, 0)));
            System.out.println("input=\"" + value + "\" limit=-1 "
                    + SplitFields.describe(SplitFields.literalDot(value, -1)));
        }
        System.out.println("limit=2: " + SplitFields.describe(SplitFields.literalDot("A.B.C", 2)));
    }
}
