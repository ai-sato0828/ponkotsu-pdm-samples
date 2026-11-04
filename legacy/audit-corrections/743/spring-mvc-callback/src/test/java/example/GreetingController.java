package example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
final class GreetingController {
    private int callCount;

    @GetMapping("/greeting")
    public String greet() {
        callCount++;
        return "Hello";
    }

    int callCount() {
        return callCount;
    }
}
