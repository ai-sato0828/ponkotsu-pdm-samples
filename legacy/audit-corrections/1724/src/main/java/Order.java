public class Order {
    private boolean canceled;

    public boolean isCanceled() {
        return canceled;
    }

    public void cancel() {
        canceled = true;
    }
}
