package info.syachiku.discount;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class DiscountAllocator {
    public Map<String, Integer> allocate(List<OrderLine> lines, int couponYen) {
        var ids = new HashSet<String>();
        long orderSubtotal = 0;
        for (OrderLine line : lines) {
            if (!ids.add(line.id())) {
                throw new IllegalArgumentException("明細IDが重複しています");
            }
            orderSubtotal += line.subtotalYen();
        }
        if (orderSubtotal == 0 || couponYen < 0 || couponYen > orderSubtotal) {
            throw new IllegalArgumentException("注文小計とクーポン額を確認してください");
        }

        var allocations = new HashMap<String, Integer>();
        var shares = new ArrayList<Share>();
        int allocated = 0;
        for (OrderLine line : lines) {
            long weighted = (long) couponYen * line.subtotalYen();
            int base = (int) (weighted / orderSubtotal);
            allocations.put(line.id(), base);
            allocated += base;
            shares.add(new Share(line.id(), weighted % orderSubtotal));
        }

        shares.sort(Comparator.comparingLong(Share::remainder)
                .reversed().thenComparing(Share::lineId));
        int remaining = couponYen - allocated;
        for (int i = 0; i < remaining; i++) {
            String id = shares.get(i).lineId();
            allocations.put(id, allocations.get(id) + 1);
        }
        return Map.copyOf(allocations);
    }

    private record Share(String lineId, long remainder) {
    }
}
