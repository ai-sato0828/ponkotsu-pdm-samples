package info.syachiku.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "orders.mapping=combined")
@AutoConfigureMockMvc
class CombinedOrdersHttpTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void listsAllWithoutStatus() throws Exception {
        mvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("findOrders"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].number").value("ORD-101"))
                .andExpect(jsonPath("$[1].number").value("ORD-102"));
    }

    @Test
    void findsOpenOrdersWhenStatusIsPresent() throws Exception {
        mvc.perform(get("/orders").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("findOrders"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].number").value("ORD-101"));
    }

    @Test
    void emptyStatusIsPresentAndMatchesNoOrders() throws Exception {
        mvc.perform(get("/orders").param("status", ""))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("findOrders"))
                .andExpect(jsonPath("$.length()").value(0));
    }
}
