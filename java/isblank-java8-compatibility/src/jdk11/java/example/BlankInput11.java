package example;

public final class BlankInput11 {
    private BlankInput11() {
    }

    public static boolean isMissing(String value) {
        return value == null || value.isBlank();
    }

    public static void main(String[] args) {
        BlankInputCheck.verifyAgainst(BlankInput11::isMissing);
    }
}
