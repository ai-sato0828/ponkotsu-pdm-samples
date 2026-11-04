package info.syachiku.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderSearchTest {
    @Autowired
    MockMvc mvc;

    @Test
    void twoIndependentConditionsCanUseRequestParam() throws Exception {
        mvc.perform(get("/demo/orders/simple?keyword=Coffee&status=CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void validQueryParametersFindSameOrderBeforeAndAfter() throws Exception {
        String query = "?keyword=Coffee&status=OPEN&fromDate=2026-10-01"
                + "&toDate=2026-10-02&staffId=S01&page=1";

        mvc.perform(get("/demo/orders/before" + query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
        mvc.perform(get("/orders" + query))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/list"))
                .andExpect(model().attribute("orders", hasSize(1)))
                .andExpect(content().string(containsString("<li>1 Coffee</li>")))
                .andExpect(content().string(containsString("name=\"keyword\" value=\"Coffee\"")))
                .andExpect(content().string(containsString("name=\"staffId\" value=\"S01\"")));
    }

    @Test
    void omittedAndEmptyPageBothUsePageOne() throws Exception {
        mvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<li>1 Coffee</li>")));
        mvc.perform(get("/orders?page=&keyword="))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<li>1 Coffee</li>")));
        mvc.perform(get("/orders?page=2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<li>3 Coffee</li>")));
    }

    @Test
    void invalidDateIs400BeforeAndReturnsFormWithInputAfter() throws Exception {
        mvc.perform(get("/demo/orders/before?fromDate=bad"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/orders?keyword=Coffee&fromDate=bad"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/list"))
                .andExpect(model().attributeHasFieldErrorCode("searchForm", "fromDate", "typeMismatch"))
                .andExpect(model().attributeDoesNotExist("orders"))
                .andExpect(content().string(containsString("name=\"keyword\" value=\"Coffee\"")))
                .andExpect(content().string(containsString("name=\"fromDate\" value=\"bad\"")))
                .andExpect(content().string(containsString("開始日はYYYY-MM-DD形式の有効な日付を入力してください")));
    }

    @Test
    void reversedDateRangeIsDisplayedOnToDate() throws Exception {
        mvc.perform(get("/orders?fromDate=2026-10-03&toDate=2026-10-02"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("searchForm", "toDate", "date.range"))
                .andExpect(model().attributeDoesNotExist("orders"))
                .andExpect(content().string(containsString("終了日は開始日以降にしてください")));
    }

    @Test
    void zeroPageIsRejectedBeforeSearching() throws Exception {
        mvc.perform(get("/orders?page=0"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("searchForm", "page", "Min"))
                .andExpect(model().attributeDoesNotExist("orders"))
                .andExpect(content().string(containsString("ページは1以上で入力してください")));
    }
}
