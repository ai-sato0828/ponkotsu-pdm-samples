package info.syachiku.auditvalidation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
class EmployeeSearchController {
    @GetMapping
    String search(
            @RequestParam("keyword") @NotBlank @Size(max = 50) String keyword,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page) {
        return keyword + ":" + page;
    }

    @GetMapping("/{code}")
    String find(@PathVariable("code") @Size(min = 32, max = 32) String code) {
        return code;
    }
}
