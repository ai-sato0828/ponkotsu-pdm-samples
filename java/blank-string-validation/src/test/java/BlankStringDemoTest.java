import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlankStringDemoTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\u3000", "\t", "\n"})
    void 氏名の空入力を拒否する(String value) {
        assertTrue(BlankStringDemo.isMissing(value));
    }

    @Test
    void 文字を含む入力を受け付ける() {
        assertFalse(BlankStringDemo.isMissing(" A "));
    }

    @Test
    void NBSPだけの入力はこの判定では未入力にならない() {
        assertFalse(BlankStringDemo.isMissing("\u00A0"));
    }

    @Test
    void nullにStringの判定を直接呼ぶと例外になる() {
        String value = null;
        assertThrows(NullPointerException.class, () -> value.isEmpty());
        assertThrows(NullPointerException.class, () -> value.isBlank());
    }

    @Test
    void 全角空白はstripだけが取り除く() {
        assertEquals("\u3000", "\u3000".trim());
        assertEquals("", "\u3000".strip());
    }

    @Test
    void NBSPはtrimとstripのどちらにも残る() {
        assertEquals("\u00A0", "\u00A0".trim());
        assertEquals("\u00A0", "\u00A0".strip());
    }
}
