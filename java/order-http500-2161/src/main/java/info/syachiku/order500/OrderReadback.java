package info.syachiku.order500;

import java.util.List;

public record OrderReadback(String sourceRequestId, int count, List<OrderView> orders) {
}
