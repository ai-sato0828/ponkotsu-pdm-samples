package info.syachiku.quantity;

import org.springframework.stereotype.Service;

@Service
public class OrderService {
    // Demonstration boundary only. No persistence, arithmetic, or quantity defaulting.
    public OrderCommand accept(OrderCommand command) {
        return command;
    }
}
