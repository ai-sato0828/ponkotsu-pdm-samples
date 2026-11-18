package example.deadline;

import java.time.LocalDate;

final class DeliveryWindow {
    private DeliveryWindow() {}

    static boolean contains(LocalDate deliveredOn, LocalDate requestedOn, long daysAfterDelivery) {
        LocalDate lastDay = deliveredOn.plusDays(daysAfterDelivery);
        return !requestedOn.isBefore(deliveredOn)
                && !requestedOn.isAfter(lastDay);
    }
}
