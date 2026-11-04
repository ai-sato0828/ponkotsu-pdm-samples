package example;

public final class RequestNumberService {
    private int lastNumber;

    public int issue() {
        return ++lastNumber;
    }
}
