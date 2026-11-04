import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

enum State { OPEN, IN_PROGRESS, DONE }
record Ticket(long id, State state, LocalDate dueDate) {}

class TicketOrder {
    static int priority(State state) {
        return switch (state) {
            case OPEN -> 0;
            case IN_PROGRESS -> 1;
            case DONE -> 2;
        };
    }

    static final Comparator<Ticket> ORDER =
            Comparator.comparingInt((Ticket t) -> priority(t.state()))
                    .thenComparing(Ticket::dueDate,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingLong(Ticket::id);

    static final Comparator<Ticket> ORDER_DESCENDING_DATE =
            Comparator.comparingInt((Ticket t) -> priority(t.state()))
                    .thenComparing(Ticket::dueDate,
                            Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparingLong(Ticket::id);

    public static void main(String[] args) {
        List<Ticket> tickets = new ArrayList<>(List.of(
                new Ticket(4, State.OPEN, null),
                new Ticket(2, State.IN_PROGRESS, LocalDate.of(2026, 9, 24)),
                new Ticket(3, State.OPEN, LocalDate.of(2026, 9, 23)),
                new Ticket(1, State.OPEN, LocalDate.of(2026, 9, 23)),
                new Ticket(5, State.OPEN, LocalDate.of(2026, 9, 25))));
        tickets.sort(ORDER);
        System.out.println(tickets.stream().map(Ticket::id).toList());
        // [1, 3, 5, 4, 2]
    }
}
