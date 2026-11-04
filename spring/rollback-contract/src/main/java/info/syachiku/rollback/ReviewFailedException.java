package info.syachiku.rollback;

public class ReviewFailedException extends RuntimeException {
    public ReviewFailedException(String message) {
        super(message);
    }
}
