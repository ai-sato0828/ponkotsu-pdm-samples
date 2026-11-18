package info.syachiku.quantity;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@Execution(ExecutionMode.SAME_THREAD)
class QuantityBoundaryTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired RecordingService recording;

    @TestConfiguration
    static class RecordingConfiguration {
        @Bean @Primary
        RecordingService recordingService() { return new RecordingService(); }
    }

    // Test-only observer of the real Service boundary; no Mockito or production counter.
    static class RecordingService extends OrderService {
        final List<OrderCommand> accepted = new ArrayList<>();
        @Override
        public OrderCommand accept(OrderCommand command) {
            accepted.add(command);
            return super.accept(command);
        }
    }

    @BeforeEach
    void resetServiceObservation() { recording.accepted.clear(); }

    static Stream<Arguments> httpCases() {
        return Stream.of(
                Arguments.of("MIN_ONLY", "/api/min-only/orders", "omitted", "{\"customerId\":10}", null, 200, null),
                Arguments.of("MIN_ONLY", "/api/min-only/orders", "null", "{\"customerId\":10,\"quantity\":null}", null, 200, null),
                Arguments.of("MIN_ONLY", "/api/min-only/orders", "zero", "{\"customerId\":10,\"quantity\":0}", 0, 400, "Min"),
                Arguments.of("MIN_ONLY", "/api/min-only/orders", "one", "{\"customerId\":10,\"quantity\":1}", 1, 200, null),
                Arguments.of("REQUIRED", "/api/required/orders", "omitted", "{\"customerId\":10}", null, 400, "NotNull"),
                Arguments.of("REQUIRED", "/api/required/orders", "null", "{\"customerId\":10,\"quantity\":null}", null, 400, "NotNull"),
                Arguments.of("REQUIRED", "/api/required/orders", "zero", "{\"customerId\":10,\"quantity\":0}", 0, 400, "Min"),
                Arguments.of("REQUIRED", "/api/required/orders", "one", "{\"customerId\":10,\"quantity\":1}", 1, 200, null)
        );
    }

    @ParameterizedTest
    @MethodSource("httpCases")
    void sameJsonShowsValidationAndServiceBoundary(
            String variant, String path, String label, String json,
            Integer expectedBoundQuantity, int expectedStatus, String expectedCode) throws Exception {
        var result = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json)).andReturn();
        assertEquals(expectedStatus, result.getResponse().getStatus());
        if (expectedStatus == 200) {
            assertNull(result.getResolvedException());
            assertEquals(List.of(new OrderCommand(10L, expectedBoundQuantity)), recording.accepted);
            var body = mapper.readTree(result.getResponse().getContentAsString());
            assertEquals(10, body.get("customerId").asLong());
            if (expectedBoundQuantity == null) assertTrue(body.get("quantity").isNull());
            else assertEquals(expectedBoundQuantity.intValue(), body.get("quantity").asInt());
        } else {
            assertTrue(recording.accepted.isEmpty());
            var failure = assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException());
            assertEquals(1, failure.getBindingResult().getFieldErrorCount());
            var error = failure.getBindingResult().getFieldError("quantity");
            assertNotNull(error);
            assertEquals(expectedCode, error.getCode());
            assertEquals(expectedBoundQuantity, error.getRejectedValue());
            Object target = failure.getBindingResult().getTarget();
            Integer bound = target instanceof OrderController.RequiredOrder required
                    ? required.quantity() : ((OrderController.MinOnlyOrder) target).quantity();
            assertEquals(expectedBoundQuantity, bound);
        }
        System.out.printf("%s input=%s bound=%s http=%d service=%d field=%s%n",
                variant, label, expectedBoundQuantity, expectedStatus, recording.accepted.size(),
                expectedCode == null ? "none" : "quantity:" + expectedCode);
    }

    static Stream<Arguments> nullJsonCases() {
        return Stream.of(
                Arguments.of("omitted", "{\"customerId\":10}"),
                Arguments.of("null", "{\"customerId\":10,\"quantity\":null}")
        );
    }

    @ParameterizedTest
    @MethodSource("nullJsonCases")
    void jsonOmissionAndExplicitNullBothBindToIntegerNullBeforeValidation(
            String label, String json) throws Exception {
        var before = mapper.readValue(json, OrderController.MinOnlyOrder.class);
        var after = mapper.readValue(json, OrderController.RequiredOrder.class);
        assertEquals(10L, before.customerId()); assertEquals(10L, after.customerId());
        assertNull(before.quantity()); assertNull(after.quantity());
        assertTrue(recording.accepted.isEmpty());
        System.out.printf("BIND input=%s minOnly=null required=null service=0%n", label);
    }
}
