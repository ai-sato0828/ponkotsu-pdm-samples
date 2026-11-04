import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRegistrationTest {
    private final UserRegistration registration = new UserRegistration();

    @Test
    void rejectsMissingRequestBeforeReadingUserId() {
        assertThrows(IllegalArgumentException.class, () -> registration.register(null));
        assertEquals(0, registration.savedCount());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsMissingOrBlankId(String id) {
        assertThrows(IllegalArgumentException.class,
                () -> registration.register(new UserRegistration.UserRequest(id, "佐藤")));
        assertEquals(0, registration.savedCount());
    }

    @Test
    void keepsMissingNicknameInStorageAndUsesFallbackForDisplay() {
        var user = registration.register(new UserRegistration.UserRequest("U001", null));

        assertEquals(1, registration.savedCount());
        assertNull(user.nickname());
        assertEquals("未設定", user.displayName());
    }
}
