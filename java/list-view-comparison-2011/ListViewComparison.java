import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListViewComparison {
    public static void main(String[] args) {
        List<String> source = new ArrayList<>(List.of("A"));
        List<String> view = Collections.unmodifiableList(source);
        List<String> snapshot = List.copyOf(view);

        source.add("B");
        check(view.equals(List.of("A", "B")), "viewに元リストの追加が反映される");
        check(snapshot.equals(List.of("A")), "snapshotには追加が反映されない");
        System.out.println("view = " + view);
        System.out.println("snapshot = " + snapshot);

        List<String> sourceWithNull = new ArrayList<>(Arrays.asList("A", null));
        boolean nullRejected = false;
        try {
            List.copyOf(sourceWithNull);
        } catch (NullPointerException expected) {
            nullRejected = true;
        }
        check(nullRejected, "List.copyOfはnull要素を拒否する");
        System.out.println("List.copyOf(sourceWithNull) = NullPointerException");

        List<String> nullSnapshot =
                Collections.unmodifiableList(new ArrayList<>(sourceWithNull));
        sourceWithNull.set(1, "B");
        check(sourceWithNull.equals(List.of("A", "B")), "元のnullをBへ差し替えた");
        check(nullSnapshot.equals(Arrays.asList("A", null)), "コピー側には元のnullが残る");
        System.out.println("sourceWithNull after set = " + sourceWithNull);
        System.out.println("nullSnapshot = " + nullSnapshot);

        boolean addRejected = false;
        try {
            nullSnapshot.add("C");
        } catch (UnsupportedOperationException expected) {
            addRejected = true;
        }
        check(addRejected, "戻されたリストへは追加できない");
        check(nullSnapshot.equals(Arrays.asList("A", null)), "追加拒否後も内容は変わらない");
        System.out.println("nullSnapshot.add = UnsupportedOperationException");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
