interface Sender {
    void send(String text);
}

class NotificationService {
    private final Sender sender;

    NotificationService(Sender sender) {
        this.sender = sender;
    }

    public String notify(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("件名が必要です");
        }
        sender.send(format(subject));
        return "送信済み";
    }

    private String format(String subject) {
        return "通知: " + subject;
    }
}
