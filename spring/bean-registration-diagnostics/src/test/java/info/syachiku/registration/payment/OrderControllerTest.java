package info.syachiku.registration.payment;

import info.syachiku.registration.payment.sales.OrderService;
import info.syachiku.registration.payment.web.OrderController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@ContextConfiguration(classes = OrderControllerTest.SliceApplication.class)
@Import(OrderControllerTest.TestDependencies.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void MVCスライスへテスト用の依存を明示するとControllerを検証できる() throws Exception {
        mvc.perform(get("/orders/42/authorization"))
                .andExpect(status().isOk())
                .andExpect(content().string("TEST:42"));
    }

    @SpringBootApplication
    static class SliceApplication {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestDependencies {
        @Bean
        OrderService orderService() {
            return new OrderService(orderId -> "TEST:" + orderId);
        }
    }
}
