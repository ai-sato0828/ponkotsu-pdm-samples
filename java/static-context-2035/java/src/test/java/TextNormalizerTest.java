import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TextNormalizerTest {

    @Test
    void 入力文字列から前後の空白を除き大文字にする() {
        assertEquals("JAVA", TextNormalizer.normalize(" java "));
    }

    @Test
    void null入力は空文字にする() {
        assertEquals("", TextNormalizer.normalize(null));
    }
}
