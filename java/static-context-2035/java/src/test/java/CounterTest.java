import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CounterTest {

    @Test
    void カウンターごとに値を保持する() {
        Counter first = new Counter();
        Counter second = new Counter();

        first.increment();
        first.increment();
        second.increment();

        assertEquals(2, first.getCount());
        assertEquals(1, second.getCount());
    }
}
