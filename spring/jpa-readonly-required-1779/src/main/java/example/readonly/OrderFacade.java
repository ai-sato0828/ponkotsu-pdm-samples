package example.readonly;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class OrderFacade {
    private final OrderWriter writer;

    OrderFacade(OrderWriter writer) {
        this.writer = writer;
    }

    @Transactional(readOnly = true)
    public OrderWriter.Observation readOnlyShip(long id) {
        return writer.ship(id);
    }

    @Transactional
    public OrderWriter.Observation writeShip(long id) {
        return writer.ship(id);
    }
}
