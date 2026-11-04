package example.dispatch;

import java.util.Comparator;
import java.util.List;

final class BrokenDispatchPreview {
    static List<String> sortedCodes(List<String> pending, int from, int to) {
        List<String> preview = pending.subList(from, to);
        preview.sort(Comparator.naturalOrder());
        return preview;
    }
}
