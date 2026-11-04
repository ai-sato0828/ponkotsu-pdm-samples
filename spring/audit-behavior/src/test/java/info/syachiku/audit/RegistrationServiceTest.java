package info.syachiku.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class RegistrationServiceTest {
    @Autowired
    private RegistrationService service;
    @Autowired
    private RegisteredUserRepository users;
    @Autowired
    private UserProfileRepository profiles;

    @BeforeEach
    void occupiedHandle() {
        profiles.deleteAll();
        users.deleteAll();
        service.register("既存ユーザー", "taken");
    }

    @Test
    void 個々の保存でコミットされプロフィールだけ失敗する() {
        assertThrows(DataIntegrityViolationException.class,
                () -> service.registerWithoutTransaction("新規ユーザー", "taken"));

        assertEquals(2, users.count());
        assertEquals(1, profiles.count());
    }

    @Test
    void 一つのサービス境界ならユーザー保存もロールバックされる() {
        assertThrows(DataIntegrityViolationException.class,
                () -> service.register("新規ユーザー", "taken"));

        assertEquals(1, users.count());
        assertEquals(1, profiles.count());
    }
}
