package example.dispatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class DispatchPreview {
    static List<String> sortedCodes(List<String> pending, int from, int to) {
        List<String> preview = new ArrayList<>(pending.subList(from, to));
        preview.sort(Comparator.naturalOrder());
        return preview;
    }
}
