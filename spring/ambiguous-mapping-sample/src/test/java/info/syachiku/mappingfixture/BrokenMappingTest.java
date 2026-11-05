package info.syachiku.mappingfixture;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrokenMappingTest {
    @Test
    void duplicateGetMappingsFailDuringStartup() {
        new WebApplicationContextRunner()
                .withUserConfiguration(BrokenApp.class)
                .run(context -> {
                    Throwable failure = context.getStartupFailure();
                    assertNotNull(failure);
                    while (failure.getCause() != null) {
                        failure = failure.getCause();
                    }
                    assertInstanceOf(IllegalStateException.class, failure);
                    String message = failure.getMessage();
                    assertTrue(message.contains("Ambiguous mapping"), message);
                    assertTrue(message.contains("{GET [/orders]}"), message);
                    assertTrue(message.contains("#list()"), message);
                    assertTrue(message.contains("#search(String)"), message);
                    System.out.println(failure);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @Import(BrokenOrdersController.class)
    static class BrokenApp {
    }

    @RestController
    static class BrokenOrdersController {
        @GetMapping("/orders")
        String list() {
            return "list";
        }

        @GetMapping("/orders")
        String search(@RequestParam(name = "status", required = false) String status) {
            return "search " + status;
        }
    }
}
