package example.deadline;

import java.time.LocalDate;

public final class DeadlineDemo {
    private DeadlineDemo() {}

    public static void main(String[] args) {
        LocalDate delivery = LocalDate.of(2026, 11, 1);
        LocalDate november9 = delivery.plusDays(8);
        LocalDate beforeDelivery = delivery.minusDays(1);
        LocalDate lastReturnDay = delivery.plusDays(14);
        LocalDate firstExpiredReturnDay = delivery.plusDays(15);
        LocalDate lastReportDay = delivery.plusDays(7);

        printShared("legacy shared 7 days", new SharedDeadline(7), delivery, november9);
        printShared("wrong shared 14 days", new SharedDeadline(14), delivery, november9);

        System.out.printf("separate: return %s=%b, missing %s=%b%n",
                november9, ReturnDeadline.accepts(delivery, november9),
                november9, MissingItemDeadline.accepts(delivery, november9));
        System.out.printf("return: %s=%b, %s=%b%n",
                lastReturnDay, ReturnDeadline.accepts(delivery, lastReturnDay),
                firstExpiredReturnDay, ReturnDeadline.accepts(delivery, firstExpiredReturnDay));
        System.out.printf("missing: %s=%b, %s=%b%n",
                lastReportDay, MissingItemDeadline.accepts(delivery, lastReportDay),
                november9, MissingItemDeadline.accepts(delivery, november9));
        System.out.printf("before delivery %s: return=%b, missing=%b%n",
                beforeDelivery, ReturnDeadline.accepts(delivery, beforeDelivery),
                MissingItemDeadline.accepts(delivery, beforeDelivery));
    }

    private static void printShared(String label, SharedDeadline deadline,
                                    LocalDate delivery, LocalDate receivedOn) {
        System.out.printf("%s: return %s=%b, missing %s=%b%n",
                label, receivedOn, deadline.acceptsReturn(delivery, receivedOn),
                receivedOn, deadline.acceptsMissingItemReport(delivery, receivedOn));
    }

    // 変更前と、共通の設定を14日に延ばした場合を再現する。
    record SharedDeadline(long daysAfterDelivery) {
        boolean acceptsReturn(LocalDate deliveredOn, LocalDate requestedOn) {
            return DeliveryWindow.contains(deliveredOn, requestedOn, daysAfterDelivery);
        }

        boolean acceptsMissingItemReport(LocalDate deliveredOn, LocalDate reportedOn) {
            return DeliveryWindow.contains(deliveredOn, reportedOn, daysAfterDelivery);
        }
    }
}
