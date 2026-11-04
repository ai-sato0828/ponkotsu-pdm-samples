import java.util.List;
import java.util.ArrayList;

final class UserRegistration {
    record UserRequest(String userId, String nickname) {}
    record User(String userId, String nickname) {
        String displayName() {
            return nickname == null ? "未設定" : nickname;
        }
    }

    private final List<User> users = new ArrayList<>();

    User register(UserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("リクエストが必要です");
        }
        String userId = request.userId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("ユーザーIDが必要です");
        }

        User user = new User(userId, request.nickname());
        users.add(user);
        return user;
    }

    int savedCount() {
        return users.size();
    }
}
