import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderBatchTest {
    @Test
    void 元の明細に追加しても確定時の明細は増えない() {
        var source = new ArrayList<>(List.of(new OrderLine(1)));
        var batch = new OrderBatch(source);

        source.add(new OrderLine(2));

        assertEquals(1, batch.getLines().size());
    }

    @Test
    void 元の明細の差し替えは確定済みの一覧に反映されない() {
        var source = new ArrayList<>(List.of(new OrderLine(1)));
        var batch = new OrderBatch(source);

        source.set(0, new OrderLine(3));

        assertEquals(1, batch.getLines().get(0).getQuantity());
    }

    @Test
    void getterで受け取った明細には追加できない() {
        var batch = new OrderBatch(List.of(new OrderLine(1)));

        assertThrows(UnsupportedOperationException.class,
                () -> batch.getLines().add(new OrderLine(2)));
    }

    @Test
    void 明細オブジェクトそのものの変更は共有される() {
        var line = new OrderLine(1);
        var batch = new OrderBatch(List.of(line));

        line.setQuantity(3);

        assertSame(line, batch.getLines().get(0));
        assertEquals(3, batch.getLines().get(0).getQuantity());
    }

    @Test
    void 明細にnullを含む入力は生成時に拒否する() {
        var source = new ArrayList<OrderLine>();
        source.add(null);

        assertThrows(NullPointerException.class, () -> new OrderBatch(source));
    }
}
