package info.syachiku.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class FailureAlertService {
    private final NotificationSender notificationSender;

    public FailureAlertService(
            @Qualifier("incident-channel") NotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }

    public String notifyFailure(String incidentId) {
        return notificationSender.send(incidentId);
    }
}
