import java.util.*;

public class Aggregate {
    public static void main(String[] args) {
        List<OrderDetail> details = List.of(new OrderDetail("A"), new OrderDetail("A"));

        Map<OrderNo, List<OrderDetail>> detailMap = new HashMap<>();

        for (OrderDetail detail : details) {
            OrderNo orderNo = new OrderNo(detail.getOrderNo());
            detailMap.computeIfAbsent(orderNo, key -> new ArrayList<>()).add(detail);
        }
        System.out.println(detailMap.get(new OrderNo("A")).size());
    }
}

record OrderNo(String value) {}
record OrderDetail(String orderNo) {
    String getOrderNo() { return orderNo; }
}
