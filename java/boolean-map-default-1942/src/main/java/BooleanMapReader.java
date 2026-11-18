import java.util.Map;

public final class BooleanMapReader {
    private BooleanMapReader() {}

    // Before: 未登録にだけfalseを補い、登録済みnullはbooleanへの変換で落ちる。
    public static boolean readWithDefault(Map<String, Boolean> flags, String key) {
        return flags.getOrDefault(key, Boolean.FALSE);
    }

    // After: この用途では、未登録・登録済みnull・falseをすべてfalseとして扱う。
    public static boolean readTrueOnly(Map<String, Boolean> flags, String key) {
        return Boolean.TRUE.equals(flags.get(key));
    }
}
