package example.deadline;

import java.time.LocalDate;

public final class MissingItemDeadline {
    private static final long DAYS_AFTER_DELIVERY = 7;

    private MissingItemDeadline() {}

    public static boolean accepts(LocalDate deliveredOn, LocalDate reportedOn) {
        return DeliveryWindow.contains(deliveredOn, reportedOn, DAYS_AFTER_DELIVERY);
    }
}
