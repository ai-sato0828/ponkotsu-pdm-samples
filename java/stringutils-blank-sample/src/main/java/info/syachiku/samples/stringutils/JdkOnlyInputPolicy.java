package info.syachiku.samples.stringutils;

public final class JdkOnlyInputPolicy {
    private JdkOnlyInputPolicy() { }

    public static boolean isDisplayNameMissing(String name) {
        return name == null || name.isBlank();
    }

    public static String memoOrDefault(String memo) {
        return memo == null || memo.isEmpty() ? "(未設定)" : memo;
    }
}
