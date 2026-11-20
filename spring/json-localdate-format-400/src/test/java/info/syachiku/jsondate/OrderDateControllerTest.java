package info.syachiku.jsondate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(classes = OrderDateControllerTest.SampleApplication.class)
@AutoConfigureMockMvc
class OrderDateControllerTest {
    private static final LocalDate EXPECTED = LocalDate.of(2026, 11, 24);

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(OrderDateController.class)
    static class SampleApplication {}

    @Autowired MockMvc mvc;

    @Test
    void getSlashUsesDateTimeFormat() throws Exception {
        assertReceived("get-slash", "getByDate",
                mvc.perform(get("/orders").queryParam("orderDate", "2026/11/24")));
    }

    @Test
    void postSlashWithDateTimeFormatDoesNotEnterController() throws Exception {
        assertUnreadable("post-slash-with-date-time-format", "postIso",
                postJson("/orders/iso", "2026/11/24"));
    }

    @Test
    void postIsoWithSameDtoReceivesLocalDate() throws Exception {
        assertReceived("post-iso", "postIso", postJson("/orders/iso", "2026-11-24"));
    }

    @Test
    void postSlashWithJsonFormatReceivesLocalDate() throws Exception {
        assertReceived("post-slash-with-json-format", "postSlash",
                postJson("/orders/slash", "2026/11/24"));
    }

    @Test
    void slashContractRejectsNonexistentDayInsteadOfCorrectingIt() throws Exception {
        assertUnreadable("post-slash-nonexistent-day", "postSlash",
                postJson("/orders/slash", "2026/02/29"));
    }

    @Test
    void slashContractAlsoRejectsIsoInput() throws Exception {
        assertUnreadable("post-iso-to-slash-contract", "postSlash",
                postJson("/orders/slash", "2026-11-24"));
    }

    private ResultActions postJson(String path, String input) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderDate\":\"" + input + "\"}"));
    }

    private void assertReceived(String scenario, String method, ResultActions request) throws Exception {
        MvcResult result = request.andExpect(handler().methodName(method))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderDate").value("2026-11-24"))
                .andReturn();
        var received = result.getRequest().getAttribute(OrderDateController.RECEIVED_ORDER_DATE);
        assertInstanceOf(LocalDate.class, received);
        assertEquals(EXPECTED, received);
        assertNull(result.getResolvedException());
        report(scenario, result, received);
    }

    private void assertUnreadable(String scenario, String method, ResultActions request) throws Exception {
        MvcResult result = request.andExpect(handler().methodName(method))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertInstanceOf(HttpMessageNotReadableException.class, result.getResolvedException());
        var received = result.getRequest().getAttribute(OrderDateController.RECEIVED_ORDER_DATE);
        assertNull(received, "マッピングは一致するが本文の読み取りに失敗し、Controllerは実行されない");
        report(scenario, result, received);
    }

    private void report(String scenario, MvcResult result, Object received) {
        var exception = result.getResolvedException();
        System.out.printf("%s: status=%d, entered=%b, received=%s, exception=%s%n",
                scenario, result.getResponse().getStatus(), received != null,
                received == null ? "-" : received,
                exception == null ? "-" : exception.getClass().getSimpleName());
    }
}
