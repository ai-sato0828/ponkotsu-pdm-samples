import java.util.Optional;
import java.util.function.Function;

public class UserLookup {
    private final Function<String, Optional<String>> search;

    UserLookup(Function<String, Optional<String>> search) {
        this.search = search;
    }

    /**
     * 利用者IDに対応する表示名を返す。
     *
     * @param userId 利用者ID。前後の空白は除く。
     * @return 利用者が存在しない場合は空のOptional
     * @throws IllegalArgumentException 利用者IDがnullまたは空白の場合
     */
    public Optional<String> findDisplayName(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        // 連携元の端末は、ID欄の両端に空白を付ける場合がある。
        return search.apply(userId.strip());
    }
}
