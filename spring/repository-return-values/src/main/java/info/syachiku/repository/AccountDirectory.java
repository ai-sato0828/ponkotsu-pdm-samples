package info.syachiku.repository;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountDirectory {
    private final AccountRepository accounts;

    public AccountDirectory(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public Optional<String> searchEmail(String email) {
        return accounts.findOptionalByEmail(email).map(Account::getEmail);
    }

    public Account require(long id) {
        return accounts.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }
}
