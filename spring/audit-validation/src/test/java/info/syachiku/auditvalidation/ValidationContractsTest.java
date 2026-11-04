package info.syachiku.auditvalidation;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ValidationContractsTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void 件名必須と壊れたJSONでは異なるエラーコードになる() throws Exception {
        json("/tasks", "{\"title\":\"確認\"}").andExpect(status().isNoContent());
        json("/tasks", "{\"title\":\"\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields.title").value("件名を入力してください"))
                .andExpect(jsonPath("$.globalErrors").isEmpty());
        json("/tasks", "{\"title\":").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_JSON"));
    }

    @Test
    void 相関制約が項目名を持たない場合もJSONへ入る() throws Exception {
        json("/tasks", """
                {"title":"確認","startDate":"2026-11-05","endDate":"2026-11-04"}
                """).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields").isEmpty())
                .andExpect(jsonPath("$.globalErrors[0]").value("開始日と終了日の順序を確認してください"));
    }

    @Test
    void 子へのValidを外すと子の入力値が検証されない() throws Exception {
        String invalid = """
                {"customerName":"田中","items":[{"sku":"A-001","quantity":2},{"sku":"","quantity":0}]}
                """;
        json("/orders/no-cascade", invalid).andExpect(status().isNoContent());
        json("/orders", invalid).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields['items[1].sku']").exists())
                .andExpect(jsonPath("$.fields['items[1].quantity']").exists());
        json("/orders", "{\"customerName\":\"田中\",\"items\":[null]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields['items[0]']").exists());
    }

    @Test
    void 同日を許可し逆転と終了日未入力を終了日のエラーへ変換する() throws Exception {
        json("/request-dates", "{\"startDate\":\"2026-11-04\",\"endDate\":\"2026-11-04\"}")
                .andExpect(status().isNoContent());
        json("/request-dates", "{\"startDate\":\"2026-11-04\",\"endDate\":\"2026-11-03\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.endDate").value("終了日は開始日以降を指定してください"));
        json("/request-dates", "{\"startDate\":\"2026-11-04\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.endDate").exists());
    }

    @Test
    void クエリの欠落と空白と型変換で例外型が変わる() throws Exception {
        mvc.perform(get("/api/employees").param("keyword", "田中").param("page", "0"))
                .andExpect(status().isOk()).andExpect(content().string("田中:0"));
        mvc.perform(get("/api/employees"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingServletRequestParameterException.class,
                        result.getResolvedException()));
        mvc.perform(get("/api/employees").param("keyword", " "))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(HandlerMethodValidationException.class,
                        result.getResolvedException()));
        mvc.perform(get("/api/employees").param("keyword", "田中").param("page", "bad"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentTypeMismatchException.class,
                        result.getResolvedException()));
    }

    @ParameterizedTest
    @MethodSource("pathLengths")
    void 文字数制約とルーティングを分けて確認する(String path, int code) throws Exception {
        mvc.perform(get(path)).andExpect(status().is(code));
    }

    static Stream<Arguments> pathLengths() {
        return Stream.of(
                Arguments.of("/api/employees/" + "a".repeat(31), 400),
                Arguments.of("/api/employees/" + "a".repeat(32), 200),
                Arguments.of("/api/employees/" + "a".repeat(33), 400),
                Arguments.of("/api/employees/" + "a".repeat(32) + "/extra", 404));
    }

    private ResultActions json(String path, String content) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(content));
    }
}
