package example.status;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class StatusController {
    @GetMapping("/orders")
    public String defaultStatus(@RequestParam("status") Status status) {
        return status.name();
    }

    @GetMapping("/orders/external")
    public String externalStatus(@RequestParam("status") String status) {
        return Status.fromValue(status)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Unsupported status"))
                .name();
    }
}
