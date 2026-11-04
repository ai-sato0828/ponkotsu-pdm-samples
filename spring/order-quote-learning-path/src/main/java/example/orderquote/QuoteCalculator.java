package example.orderquote;

import java.util.List;

public final class QuoteCalculator {
    public long total(List<LineItem> lines) {
        if (lines == null || lines.isEmpty() || lines.size() > 50) {
            throw new InvalidQuoteException("明細は1〜50件です");
        }

        long total = 0;
        for (LineItem line : lines) {
            if (line == null) {
                throw new InvalidQuoteException("明細に空の行があります");
            }
            total += line.amount();
        }
        return total;
    }
}
