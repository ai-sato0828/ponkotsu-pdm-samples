package example.orderquote;

public final class InvalidQuoteException extends RuntimeException {
    public InvalidQuoteException(String message) {
        super(message);
    }
}
