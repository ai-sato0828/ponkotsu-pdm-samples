import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class QuantityParserTest {
    @ParameterizedTest
    @CsvSource(value = {"0|0", "1234|1234", "1,234|1234", "0001|1", "000,001|1",
            "2147483647|2147483647", "2,147,483,647|2147483647"}, delimiter = '|')
    void 許可した形式を数値に変換する(String input, int expected) {
        assertEquals(expected, QuantityParser.parse(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "　"})
    void 未入力には必須のエラーを返す(String input) {
        var error = assertThrowsExactly(IllegalArgumentException.class,
                () -> QuantityParser.parse(input));
        assertEquals("数量を入力してください", error.getMessage());
        assertNull(error.getCause());
    }

    @ParameterizedTest
    @ValueSource(strings = {" 123 ", "12,34", "1,23,456", "1234,567", "1,,234",
            "１２３", "1.5", "-1", "+1"})
    void 許可しない書式には形式のエラーを返す(String input) {
        var error = assertThrowsExactly(IllegalArgumentException.class,
                () -> QuantityParser.parse(input));
        assertEquals("数量は半角数字と3桁区切りのカンマで入力してください", error.getMessage());
        assertNull(error.getCause());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2147483648", "2,147,483,648", "999999999999999999999"})
    void int上限超過には範囲のエラーと原因を返す(String input) {
        var error = assertThrowsExactly(IllegalArgumentException.class,
                () -> QuantityParser.parse(input));
        assertEquals("数量は0〜2,147,483,647で入力してください", error.getMessage());
        assertInstanceOf(NumberFormatException.class, error.getCause());
    }
}
