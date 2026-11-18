package example.deadline;

import java.time.LocalDate;

public final class ReturnDeadline {
    private static final long DAYS_AFTER_DELIVERY = 14;

    private ReturnDeadline() {}

    public static boolean accepts(LocalDate deliveredOn, LocalDate requestedOn) {
        return DeliveryWindow.contains(deliveredOn, requestedOn, DAYS_AFTER_DELIVERY);
    }
}
