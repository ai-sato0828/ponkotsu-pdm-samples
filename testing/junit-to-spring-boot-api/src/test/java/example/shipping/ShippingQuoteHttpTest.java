package example.shipping;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShippingQuoteHttpTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void returnsTheSubtotalAndFeeAsJson() throws Exception {
        mvc.perform(get("/shipping-quote").param("subtotal", "8000"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.subtotal").value(8000))
                .andExpect(jsonPath("$.shippingFee").value(500));
    }

    @Test
    void rejectsAMissingSubtotal() throws Exception {
        mvc.perform(get("/shipping-quote"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsANonIntegerSubtotal() throws Exception {
        mvc.perform(get("/shipping-quote").param("subtotal", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void translatesTheShippingRuleRejectionToBadRequest() throws Exception {
        mvc.perform(get("/shipping-quote").param("subtotal", "-1"))
                .andExpect(status().isBadRequest());
    }
}
