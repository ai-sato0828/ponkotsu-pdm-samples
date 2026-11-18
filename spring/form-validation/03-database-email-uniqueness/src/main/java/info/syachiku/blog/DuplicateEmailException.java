package info.syachiku.blog;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("Duplicate email");
    }

    public DuplicateEmailException(Throwable cause) {
        super("Duplicate email", cause);
    }
}
