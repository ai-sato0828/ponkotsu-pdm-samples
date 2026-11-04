package example.orders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static example.orders.Order.Status.CANCELED;
import static example.orders.Order.Status.SHIPPED;
import static example.orders.Order.Status.WAITING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OrderControllerTest.TestApplication.class)
@AutoConfigureMockMvc
class OrderControllerTest {
    @Autowired MockMvc mvc;
    @Autowired InMemoryOrderRepository repository;

    @BeforeEach
    void setup() {
        repository.reset(
                new Order(100, WAITING, "REF-100"),
                new Order(101, SHIPPED, "REF-101"),
                new Order(102, CANCELED, "REF-102"));
    }

    @Test
    void detailPublishesExactlyThreeFields() throws Exception {
        mvc.perform(get("/orders/100"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"id":100,"status":"WAITING","cancelable":true}
                        """, JsonCompareMode.STRICT));

        assertEquals(0, repository.saves);
    }

    @Test
    void invalidPathIdReturns400WithoutReadingOrSaving() throws Exception {
        var result = mvc.perform(post("/orders/abc/cancel"))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertInstanceOf(MethodArgumentTypeMismatchException.class, result.getResolvedException());
        assertEquals(0, repository.reads);
        assertEquals(0, repository.saves);
    }

    @Test
    void missingOrderReturns404WithoutSaving() throws Exception {
        mvc.perform(post("/orders/999/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {"code":"ORDER_NOT_FOUND"}
                        """, JsonCompareMode.STRICT));

        assertEquals(0, repository.saves);
    }

    @ParameterizedTest
    @CsvSource({"101,SHIPPED", "102,CANCELED"})
    void unavailableOrderReturns409WithoutSaving(long id, Order.Status expectedStatus) throws Exception {
        mvc.perform(post("/orders/{id}/cancel", id))
                .andExpect(status().isConflict())
                .andExpect(content().json("""
                        {"code":"ORDER_CANNOT_BE_CANCELED"}
                        """, JsonCompareMode.STRICT));

        assertEquals(expectedStatus, repository.stored(id).status());
        assertEquals(0, repository.saves);
    }

    @Test
    void waitingOrderReturnsCanceledResultAndSavesOnce() throws Exception {
        mvc.perform(post("/orders/100/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"id":100,"status":"CANCELED","cancelable":false}
                        """, JsonCompareMode.STRICT));

        assertEquals(CANCELED, repository.stored(100).status());
        assertEquals(1, repository.saves);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({OrderController.class, OrderService.class})
    static class TestApplication {
        @Bean
        InMemoryOrderRepository orderRepository() {
            return new InMemoryOrderRepository();
        }
    }
}
