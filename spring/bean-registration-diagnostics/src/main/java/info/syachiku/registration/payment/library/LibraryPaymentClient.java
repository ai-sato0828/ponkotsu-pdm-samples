package info.syachiku.registration.payment.library;

public class LibraryPaymentClient {
    public String requestAuthorization(String orderId) {
        return "SDK:" + orderId;
    }
}
