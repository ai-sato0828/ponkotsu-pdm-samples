package info.syachiku.split;

public final class Quiz {

    private Quiz() {
    }

    public static void main(String[] args) {
        String[] parts = "a.b".split(".");
        System.out.println(parts.length);
    }
}
