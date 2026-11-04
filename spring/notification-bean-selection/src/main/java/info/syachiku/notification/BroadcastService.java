package info.syachiku.notification;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BroadcastService {
    private final List<NotificationSender> senders;

    public BroadcastService(List<NotificationSender> senders) {
        this.senders = senders;
    }

    public List<String> broadcast(String message) {
        return senders.stream().map(sender -> sender.send(message)).toList();
    }
}
