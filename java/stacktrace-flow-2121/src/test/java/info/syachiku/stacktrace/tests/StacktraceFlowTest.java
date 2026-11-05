package info.syachiku.stacktrace.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.syachiku.stacktrace.source.BrokenQuantityConverter;
import info.syachiku.stacktrace.source.FixedQuantityConverter;
import info.syachiku.stacktrace.source.QuantityController;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class StacktraceFlowTest {
    private final Path evidence = Path.of("target/stacktrace-evidence");
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void builtinConversionRejectsWordBeforeControllerAndValidNumberSucceeds() throws Exception {
        var controller = new QuantityController();
        var mvc = mvc(controller, null);
        var result = mvc.perform(get("/quantity/int").param("quantity", "three"))
                .andExpect(status().isBadRequest()).andReturn();
        assertThat(controller.calls()).isZero();
        assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentTypeMismatchException.class);
        var root = root(result.getResolvedException());
        assertThat(root).isInstanceOf(NumberFormatException.class);
        assertThat(sourceFrames(root)).isEmpty();
        save("builtin-invalid", "GET /quantity/int?quantity=three", result, controller.calls());

        var corrected = mvc.perform(get("/quantity/int").param("quantity", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(2)).andReturn();
        assertThat(controller.calls()).isEqualTo(1);
        save("builtin-corrected", "GET /quantity/int?quantity=2", corrected, controller.calls());
    }

    @Test
    void customConversionFailureContainsTheExecutedApplicationSourceLine() throws Exception {
        var controller = new QuantityController();
        var mvc = mvc(controller, new BrokenQuantityConverter());
        var result = mvc.perform(get("/quantity/custom").param("quantity", " 2 "))
                .andExpect(status().isBadRequest()).andReturn();
        assertThat(controller.calls()).isZero();
        assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentTypeMismatchException.class);
        var root = root(result.getResolvedException());
        assertThat(root).isInstanceOf(NumberFormatException.class);
        var first = sourceFrames(root).get(0);
        assertThat(first.getClassName()).isEqualTo(BrokenQuantityConverter.class.getName());
        assertThat(first.getFileName()).isEqualTo("BrokenQuantityConverter.java");
        assertThat(first.getLineNumber()).isGreaterThan(0);
        String line = Files.readAllLines(Path.of("src/main/java/info/syachiku/stacktrace/source/BrokenQuantityConverter.java"))
                .get(first.getLineNumber() - 1);
        assertThat(line.strip()).isEqualTo("return new Quantity(Integer.parseInt(source));");
        save("custom-broken", "GET /quantity/custom ; quantity=\" 2 \"", result, controller.calls());
    }

    @Test
    void converterRepairAcceptsContractualWhitespaceAndPreservesNormalInput() throws Exception {
        var controller = new QuantityController();
        var mvc = mvc(controller, new FixedQuantityConverter());
        var result = mvc.perform(get("/quantity/custom").param("quantity", " 2 "))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(2)).andReturn();
        save("custom-fixed", "GET /quantity/custom ; quantity=\" 2 \"", result, controller.calls());
        mvc.perform(get("/quantity/custom").param("quantity", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(2));
        assertThat(controller.calls()).isEqualTo(2);
    }

    @Test
    void converterRepairStillRejectsNonNumericInputBeforeController() throws Exception {
        var controller = new QuantityController();
        var result = mvc(controller, new FixedQuantityConverter())
                .perform(get("/quantity/custom").param("quantity", "three"))
                .andExpect(status().isBadRequest()).andReturn();
        assertThat(controller.calls()).isZero();
        assertThat(root(result.getResolvedException())).isInstanceOf(NumberFormatException.class);
        save("custom-still-invalid", "GET /quantity/custom?quantity=three", result, controller.calls());
    }

    private MockMvc mvc(QuantityController controller, Converter<?, ?> converter) {
        var conversion = new DefaultFormattingConversionService();
        if (converter != null) conversion.addConverter(converter);
        return standaloneSetup(controller).setConversionService(conversion).build();
    }

    private Throwable root(Throwable exception) {
        var current = exception;
        while (current.getCause() != null) current = current.getCause();
        return current;
    }

    private List<StackTraceElement> sourceFrames(Throwable exception) {
        return Arrays.stream(exception.getStackTrace())
                .filter(frame -> frame.getClassName().startsWith("info.syachiku.stacktrace.source."))
                .toList();
    }

    private void save(String name, String request, MvcResult result, int controllerCalls) throws Exception {
        Files.createDirectories(evidence);
        var exception = result.getResolvedException();
        var text = new StringWriter();
        var writer = new PrintWriter(text);
        writer.println("Captured in local MockMvc verification; full resolved exception, not default server logging.");
        writer.println(request);
        writer.println("HTTP " + result.getResponse().getStatus());
        writer.println("Controller method invocations: " + controllerCalls);
        if (exception != null) exception.printStackTrace(writer);
        else writer.println("Response: " + result.getResponse().getContentAsString());
        writer.flush();
        Files.writeString(evidence.resolve(name + ".log"), text.toString(), StandardCharsets.UTF_8);
        var data = new LinkedHashMap<String, Object>();
        data.put("request", request);
        data.put("httpStatus", result.getResponse().getStatus());
        data.put("controllerInvocations", controllerCalls);
        data.put("response", result.getResponse().getContentAsString());
        var chain = new ArrayList<Map<String, Object>>();
        for (Throwable current = exception; current != null; current = current.getCause()) {
            var item = new LinkedHashMap<String, Object>();
            item.put("type", current.getClass().getName());
            item.put("message", current.getMessage());
            item.put("applicationSourceFrames", sourceFrames(current).stream().map(Object::toString).toList());
            chain.add(item);
        }
        data.put("exceptionChain", chain);
        Files.writeString(evidence.resolve(name + ".json"), json.writerWithDefaultPrettyPrinter().writeValueAsString(data));
    }
}
