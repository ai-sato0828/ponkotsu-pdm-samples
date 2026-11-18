import java.util.Optional;
import java.util.function.Supplier;

public final class OptionalDisplayName {
    private OptionalDisplayName() {}

    // 比較用: nameがnullならOptionalを作る時点で例外になる。
    public static String usingOf(String name, Supplier<String> fallback) {
        return Optional.of(name).orElseGet(fallback);
    }

    // nullを「未設定」として扱う表示名では、emptyにして代替表示へ進む。
    public static String usingOfNullable(String name, Supplier<String> fallback) {
        return Optional.ofNullable(name).orElseGet(fallback);
    }
}
