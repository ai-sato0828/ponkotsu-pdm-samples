package info.syachiku.stacktrace.source;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuantityController {
    private final AtomicInteger calls = new AtomicInteger();

    @GetMapping("/quantity/int")
    public Map<String, Integer> integer(@RequestParam("quantity") int quantity) {
        calls.incrementAndGet();
        return Map.of("quantity", quantity);
    }

    @GetMapping("/quantity/custom")
    public Map<String, Integer> custom(@RequestParam("quantity") Quantity quantity) {
        calls.incrementAndGet();
        return Map.of("quantity", quantity.value());
    }

    public int calls() {
        return calls.get();
    }
}
