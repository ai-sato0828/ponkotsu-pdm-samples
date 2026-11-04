package info.syachiku.notification;

@FunctionalInterface
public interface NotificationSender {
    String send(String message);
}
