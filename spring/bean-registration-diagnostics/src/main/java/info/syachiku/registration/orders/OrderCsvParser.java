package info.syachiku.registration.orders;

import org.springframework.stereotype.Component;

@Component
public class OrderCsvParser {
    public OrderRow parse(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length != 2 || fields[0].isBlank() || fields[1].isBlank()) {
            throw new IllegalArgumentException("注文番号と状態の2列が必要です");
        }
        return new OrderRow(fields[0], fields[1]);
    }
}
