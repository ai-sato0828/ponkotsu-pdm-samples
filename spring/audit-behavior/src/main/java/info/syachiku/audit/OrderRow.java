package info.syachiku.audit;

import java.math.BigDecimal;

public record OrderRow(Long orderId, String customerName, BigDecimal totalAmount) {
}
