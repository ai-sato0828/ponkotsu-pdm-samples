package info.syachiku.blog;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService failingInsert(RuntimeException failure) {
        return new UserService(new UserRepository() {
            public boolean existsByEmail(String email) { return false; }
            public long save(UserRegisterCommand command) { throw failure; }
        });
    }

    @Test
    void onlyTheDuplicateKeyFailureBecomesADuplicateEmailWithItsCause() {
        var databaseFailure = new DuplicateKeyException("email PK conflict");
        var failure = assertThrows(DuplicateEmailException.class,
                () -> failingInsert(databaseFailure).register(
                        new UserRegisterCommand("山田", "same@example.com", "DEV")));
        assertSame(databaseFailure, failure.getCause());
    }

    @Test
    void otherIntegrityFailuresAreNotLabeledAsDuplicateEmail() {
        var databaseFailure = new DataIntegrityViolationException("name NOT NULL");
        var failure = assertThrows(DataIntegrityViolationException.class,
                () -> failingInsert(databaseFailure).register(
                        new UserRegisterCommand(null, "new@example.com", "DEV")));
        assertSame(databaseFailure, failure);
    }
}
