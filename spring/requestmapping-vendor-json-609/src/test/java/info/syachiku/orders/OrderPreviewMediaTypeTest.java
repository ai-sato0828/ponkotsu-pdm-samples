package info.syachiku.orders;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.List;

import static info.syachiku.orders.OrderPreviewControllers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderPreviewMediaTypeTest {

    private static final String BODY = "{\"orderId\":\"ORD-42\",\"quantity\":2}";
    private static final MediaType ORDER_JSON =
            MediaType.parseMediaType("application/vnd.example.order+json");
    private static final MediaType INVOICE_JSON =
            MediaType.parseMediaType("application/vnd.example.invoice+json");

    private final RecordingService service = new RecordingService();

    @Test
    void jsonOnlyAcceptsApplicationJson() throws Exception {
        MvcResult result = assertAccepted(send(new JsonOnlyController(service), MediaType.APPLICATION_JSON));
        printOutcome("01 strict application/json", result);
    }

    @Test
    void jsonOnlyRejectsOrderVendorTypeBeforeController() throws Exception {
        MvcResult result = assertRejected(send(new JsonOnlyController(service), ORDER_JSON));
        printOutcome("02 strict order+json", result);
    }

    @Test
    void explicitOrderVendorTypeReachesController() throws Exception {
        MvcResult result = assertAccepted(send(new OrderVendorController(service), ORDER_JSON));
        printOutcome("03 explicit order+json", result);
    }

    @Test
    void explicitOrderVendorTypeStillRejectsInvoiceVendorType() throws Exception {
        MvcResult result = assertRejected(send(new OrderVendorController(service), INVOICE_JSON));
        printOutcome("04 explicit invoice+json", result);
    }

    @Test
    void defaultJacksonConverterCanReadBothVendorJsonTypes() {
        var jackson = new MappingJackson2HttpMessageConverter();
        boolean orderReadable = jackson.canRead(OrderRequest.class, ORDER_JSON);
        boolean invoiceReadable = jackson.canRead(OrderRequest.class, INVOICE_JSON);
        assertTrue(orderReadable);
        assertTrue(invoiceReadable);
        System.out.printf("Jackson canRead order+json=%s invoice+json=%s%n", orderReadable, invoiceReadable);
    }

    private ResultActions send(Object controller, MediaType requestType) throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
        return mvc.perform(post("/orders/preview")
                .contentType(requestType)
                .accept(MediaType.APPLICATION_JSON)
                .content(BODY));
    }

    private MvcResult assertAccepted(ResultActions actions) throws Exception {
        MvcResult result = actions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(BODY, JsonCompareMode.STRICT))
                .andReturn();
        assertInstanceOf(HandlerMethod.class, result.getHandler());
        assertEquals(List.of(new OrderRequest("ORD-42", 2)), service.requests);
        return result;
    }

    private MvcResult assertRejected(ResultActions actions) throws Exception {
        MvcResult result = actions.andExpect(status().isUnsupportedMediaType()).andReturn();
        assertInstanceOf(HttpMediaTypeNotSupportedException.class, result.getResolvedException());
        assertNull(result.getHandler());
        assertTrue(service.requests.isEmpty());
        return result;
    }

    private void printOutcome(String label, MvcResult result) throws Exception {
        String detail = result.getResolvedException() == null
                ? result.getResponse().getContentAsString()
                : result.getResolvedException().getClass().getSimpleName();
        System.out.printf("%s -> %d, service calls=%d, %s%n",
                label, result.getResponse().getStatus(), service.requests.size(), detail);
    }

    private static final class RecordingService implements OrderPreviewService {
        private final List<OrderRequest> requests = new ArrayList<>();

        @Override
        public OrderPreview preview(OrderRequest request) {
            requests.add(request);
            return new OrderPreview(request.orderId(), request.quantity());
        }
    }
}
