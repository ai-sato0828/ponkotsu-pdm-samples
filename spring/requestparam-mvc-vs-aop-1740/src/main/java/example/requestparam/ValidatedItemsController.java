package example.requestparam;

import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
public class ValidatedItemsController {
    @GetMapping("/items")
    public Map<String, String> items(@RequestParam("keyword") @NotBlank String keyword) {
        return Map.of("keyword", keyword);
    }
}
