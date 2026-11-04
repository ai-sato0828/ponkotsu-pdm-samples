import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TicketOrderTest {
    private final List<Ticket> tickets = List.of(
            new Ticket(4, State.OPEN, null),
            new Ticket(2, State.IN_PROGRESS, LocalDate.of(2026, 9, 24)),
            new Ticket(3, State.OPEN, LocalDate.of(2026, 9, 23)),
            new Ticket(1, State.OPEN, LocalDate.of(2026, 9, 23)),
            new Ticket(5, State.OPEN, LocalDate.of(2026, 9, 25)));

    @Test
    void ascendingDateKeepsStatePriorityNullsLastAndIdOrder() {
        assertEquals(List.of(1L, 3L, 5L, 4L, 2L),
                tickets.stream().sorted(TicketOrder.ORDER).map(Ticket::id).toList());
    }

    @Test
    void descendingDateKeepsStatePriorityNullsLastAndIdOrder() {
        assertEquals(List.of(5L, 1L, 3L, 4L, 2L),
                tickets.stream().sorted(TicketOrder.ORDER_DESCENDING_DATE).map(Ticket::id).toList());
    }
}
