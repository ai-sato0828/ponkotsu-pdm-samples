import java.util.Locale;

public class ImportSuccessRate {

    public static String format(int succeeded, int total) {
        if (succeeded < 0 || total < 0 || succeeded > total) {
            throw new IllegalArgumentException("成功件数は0以上、全件数以下です");
        }
        if (total == 0) {
            return "未集計";
        }

        double ratio = (double) succeeded / total;
        return String.format(Locale.ROOT, "%.1f%%", ratio * 100.0);
    }

    public static void main(String[] args) {
        int succeeded = 1;
        int total = 4;

        double assignedToDouble = succeeded / total;
        double castAfterDivision = (double) (succeeded / total);
        double castBeforeDivision = (double) succeeded / total;

        System.out.println("doubleへ代入: " + assignedToDouble);
        System.out.println("割った後にキャスト: " + castAfterDivision);
        System.out.println("割る前にキャスト: " + castBeforeDivision);
        System.out.println("1件成功 / 4件: " + format(succeeded, total));
        System.out.println("2件成功 / 3件: " + format(2, 3));
        System.out.println("0件成功 / 4件: " + format(0, 4));
        System.out.println("0件成功 / 0件: " + format(0, 0));
    }
}
