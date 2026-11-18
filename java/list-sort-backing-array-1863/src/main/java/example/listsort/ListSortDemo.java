package example.listsort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ListSortDemo {
    private static final List<String> IMPORT_ORDER = List.of("ORD-103", "ORD-101", "ORD-102");

    public static void main(String[] args) {
        String[] importedForView = IMPORT_ORDER.toArray(String[]::new);
        List<String> view = Arrays.asList(importedForView);
        List<String> sortedView = sortInPlace(view);
        System.out.printf("Arrays.asList before=%s sorted=%s arrayAfter=%s%n",
                IMPORT_ORDER, sortedView, Arrays.toString(importedForView));

        List<String> readOnly = List.of("ORD-103", "ORD-101", "ORD-102");
        try {
            sortInPlace(readOnly);
        } catch (UnsupportedOperationException failure) {
            System.out.printf("List.of before=%s sort=%s listAfter=%s%n",
                    IMPORT_ORDER, failure.getClass().getSimpleName(), readOnly);
        }

        String[] importedForCopy = IMPORT_ORDER.toArray(String[]::new);
        List<String> preview = sortedCopy(Arrays.asList(importedForCopy));
        System.out.printf("ArrayList copy before=%s sorted=%s arrayAfter=%s%n",
                IMPORT_ORDER, preview, Arrays.toString(importedForCopy));
    }

    static List<String> sortInPlace(List<String> orderIds) {
        orderIds.sort(Comparator.naturalOrder());
        return orderIds;
    }

    static List<String> sortedCopy(List<String> orderIds) {
        List<String> preview = new ArrayList<>(orderIds);
        preview.sort(Comparator.naturalOrder());
        return preview;
    }
}
