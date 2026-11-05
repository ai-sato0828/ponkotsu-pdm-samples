package info.syachiku.booleanpatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

/** activeの省略・明示null・真偽値を区別する、記事用の小さな更新処理。 */
public final class ActivePatchService {
    public enum Outcome { UNCHANGED, UPDATED }
    public enum InputError { INVALID_JSON, ROOT_NOT_OBJECT, ACTIVE_NOT_BOOLEAN }

    public static final class PatchInputException extends IllegalArgumentException {
        private final InputError error;

        private PatchInputException(InputError error, String message) {
            super(message);
            this.error = error;
        }

        public InputError error() {
            return error;
        }
    }

    public static final class User {
        private boolean active;

        public User(boolean active) {
            this.active = active;
        }

        public boolean active() {
            return active;
        }
    }

    @FunctionalInterface
    public interface Repository {
        void save(boolean active);
    }

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
    private final Repository repository;

    public ActivePatchService(Repository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Outcome apply(String json, User user) {
        Objects.requireNonNull(user);
        JsonNode body = readObject(json);

        // hasNonNullでは、明示nullも「項目なし」と同じになってしまう。
        if (!body.has("active")) {
            return Outcome.UNCHANGED;
        }
        JsonNode active = body.get("active");
        if (!active.isBoolean()) {
            throw new PatchInputException(InputError.ACTIVE_NOT_BOOLEAN,
                    "activeにはJSONのtrueまたはfalseを指定してください");
        }

        // 入力の確認をすべて済ませてから、状態を変えて保存する。
        user.active = active.booleanValue();
        repository.save(user.active);
        return Outcome.UPDATED;
    }

    private JsonNode readObject(String json) {
        if (json == null || json.isBlank()) {
            throw invalidJson();
        }
        final JsonNode body;
        try {
            body = mapper.readTree(json);
        } catch (JsonProcessingException error) {
            throw invalidJson();
        }
        if (body == null || !body.isObject()) {
            throw new PatchInputException(InputError.ROOT_NOT_OBJECT,
                    "入力のルートはJSONオブジェクトにしてください");
        }
        return body;
    }

    private static PatchInputException invalidJson() {
        return new PatchInputException(InputError.INVALID_JSON,
                "重複キーのないJSONを1件だけ指定してください");
    }
}
