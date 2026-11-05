package info.syachiku.window;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationWindowController {
    private final ApplicationWindow window;

    public ApplicationWindowController(ApplicationWindow window) {
        this.window = window;
    }

    @GetMapping("/application-window")
    public ApplicationWindow.Status status() {
        return window.status();
    }
}
