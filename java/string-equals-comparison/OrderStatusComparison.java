import java.util.Objects;

public class OrderStatusComparison {
    private static final String CANCELLED = "CANCELLED";

    public static void main(String[] args) {
        String cancelledFromCsv = readStatus("A-104,CANCELLED");
        String paidFromCsv = readStatus("A-105,PAID");
        String missingFromCsv = readStatus("A-106,");

        printRefundCandidate("直書きのテスト値", "CANCELLED");
        printRefundCandidate("CSVのA-104", cancelledFromCsv);
        printRefundCandidate("CSVのA-105", paidFromCsv);
        printRefundCandidate("CSVのA-106", missingFromCsv);

        System.out.println("記録済みの取消とCSVの取消の値は同じ: "
                + Objects.equals(CANCELLED, cancelledFromCsv));
        System.out.println("片方だけ未入力の場合、値は同じ: "
                + Objects.equals(CANCELLED, missingFromCsv));
        System.out.println("両方未入力の場合、値は同じ: "
                + Objects.equals(null, missingFromCsv));
        System.out.println("両方未入力の場合、記録済みの値が一致: "
                + hasSameRecordedStatus(null, missingFromCsv));
        System.out.println("両方に取消の記録がある場合、記録済みの値が一致: "
                + hasSameRecordedStatus(CANCELLED, cancelledFromCsv));
    }

    private static String readStatus(String csvRow) {
        // 練習用の2列。引用符やフィールド内のカンマは扱わない。
        String[] fields = csvRow.split(",", -1);
        return fields[1].isEmpty() ? null : fields[1];
    }

    private static void printRefundCandidate(String source, String status) {
        System.out.printf("%s（%s）: == %b / equals %b%n",
                source, status, status == CANCELLED, CANCELLED.equals(status));
    }

    private static boolean hasSameRecordedStatus(String previousStatus, String incomingStatus) {
        return previousStatus != null && previousStatus.equals(incomingStatus);
    }
}
