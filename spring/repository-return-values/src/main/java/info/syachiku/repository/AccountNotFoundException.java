package info.syachiku.repository;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(long id) {
        super("Account not found: " + id);
    }
}
