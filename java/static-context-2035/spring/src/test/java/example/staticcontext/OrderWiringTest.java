package example.staticcontext;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderWiringTest {

    @Test
    void RunnerはSpringから渡されたServiceで未処理注文を更新する() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(BatchApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.main.banner-mode=off", "logging.level.root=WARN")
                .run()) {
            OrderService service = context.getBean(OrderService.class);
            assertEquals("PROCESSED", service.find(1L).status());
            assertEquals("PROCESSED", service.find(2L).status());
            assertEquals(0, context.getBean(OrderRepository.class).findPendingOrders().size());
        }
    }

    @Test
    void 別Repositoryを渡したServiceへ処理結果が漏れない() {
        OrderService first = new OrderService(new InMemoryOrderRepository());
        OrderService second = new OrderService(new InMemoryOrderRepository());
        first.processPendingOrders();
        assertEquals("PROCESSED", first.find(1L).status());
        assertEquals("PENDING", second.find(1L).status());
    }

    @Test
    void Controllerは渡されたServiceを使い存在しないIDを404へ変換する() throws Exception {
        OrderService service = new OrderService(new InMemoryOrderRepository());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new OrderController(service)).build();
        mvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
        mvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());
    }
}
