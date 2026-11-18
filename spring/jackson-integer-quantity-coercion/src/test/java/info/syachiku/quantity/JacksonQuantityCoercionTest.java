package info.syachiku.quantity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@Import(QuantityBoundaryTest.RecordingConfiguration.class)
@Execution(ExecutionMode.SAME_THREAD)
class JacksonQuantityCoercionTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired QuantityBoundaryTest.RecordingService recording;

    @BeforeEach
    void clearServiceObservation() {
        recording.accepted.clear();
    }

    static Stream<Arguments> cases() {
        String required = "/api/required/orders";
        String strict = "/api/strict/orders";
        return Stream.of(
                Arguments.of(required, "integer", "{\"customerId\":10,\"quantity\":2}", 200, 2, null, null),
                Arguments.of(required, "numeric string", "{\"customerId\":10,\"quantity\":\"2\"}", 200, 2, null, null),
                Arguments.of(required, "non-numeric string", "{\"customerId\":10,\"quantity\":\"二個\"}", 400, null, InvalidFormatException.class, null),
                Arguments.of(required, "decimal", "{\"customerId\":10,\"quantity\":2.9}", 200, 2, null, null),
                Arguments.of(strict, "integer", "{\"customerId\":10,\"quantity\":2}", 200, 2, null, null),
                Arguments.of(strict, "numeric string", "{\"customerId\":10,\"quantity\":\"2\"}", 400, null, MismatchedInputException.class, null),
                Arguments.of(strict, "non-numeric string", "{\"customerId\":10,\"quantity\":\"二個\"}", 400, null, MismatchedInputException.class, null),
                Arguments.of(strict, "decimal", "{\"customerId\":10,\"quantity\":2.9}", 400, null, MismatchedInputException.class, null),
                Arguments.of(strict, "zero", "{\"customerId\":10,\"quantity\":0}", 400, null, null, "Min"),
                Arguments.of(strict, "null", "{\"customerId\":10,\"quantity\":null}", 400, null, null, "NotNull"),
                Arguments.of(strict, "omitted", "{\"customerId\":10}", 400, null, null, "NotNull"),
                Arguments.of(strict, "numeric string customerId", "{\"customerId\":\"10\",\"quantity\":2}", 200, 2, null, null));
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("cases")
    void verifiesHttpStatusExceptionAndActualServiceInput(
            String path, String label, String body, int expectedStatus, Integer expectedQuantity,
            Class<? extends MismatchedInputException> expectedJacksonError, String expectedValidationCode)
            throws Exception {
        var result = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
        assertEquals(expectedStatus, result.getResponse().getStatus());
        var resolved = result.getResolvedException();
        String cause = "none";
        if (expectedStatus == 200) {
            assertNull(resolved);
            assertEquals(List.of(new OrderCommand(10L, expectedQuantity)), recording.accepted);
            var response = mapper.readTree(result.getResponse().getContentAsString());
            assertTrue(response.get("quantity").isIntegralNumber());
            assertEquals(expectedQuantity.intValue(), response.get("quantity").intValue());
        } else if (expectedJacksonError != null) {
            var conversionFailure = assertInstanceOf(HttpMessageNotReadableException.class, resolved);
            var jacksonFailure = assertInstanceOf(expectedJacksonError, conversionFailure.getCause());
            assertEquals(expectedJacksonError, jacksonFailure.getClass());
            var fieldPath = jacksonFailure.getPath();
            assertEquals("quantity", fieldPath.get(fieldPath.size() - 1).getFieldName());
            assertTrue(recording.accepted.isEmpty());
            cause = jacksonFailure.getClass().getSimpleName();
        } else {
            var validationFailure = assertInstanceOf(MethodArgumentNotValidException.class, resolved);
            var fieldError = validationFailure.getBindingResult().getFieldError("quantity");
            assertNotNull(fieldError);
            assertEquals(expectedValidationCode, fieldError.getCode());
            assertTrue(recording.accepted.isEmpty());
            cause = "quantity:" + fieldError.getCode();
        }
        System.out.printf("COERCION path=%s input=%s http=%d service=%d qty=%s exception=%s cause=%s%n",
                path, label, result.getResponse().getStatus(), recording.accepted.size(),
                recording.accepted.isEmpty() ? "-" : recording.accepted.get(0).quantity(),
                resolved == null ? "none" : resolved.getClass().getSimpleName(), cause);
    }
}
