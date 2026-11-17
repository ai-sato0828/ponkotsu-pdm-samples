package info.syachiku.constraints;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/validation")
public class NameConstraintController {
    @PostMapping("/not-null")
    public AcceptedName notNull(@Valid @RequestBody NotNullName request) {
        return new AcceptedName(request.name());
    }

    @PostMapping("/not-empty")
    public AcceptedName notEmpty(@Valid @RequestBody NotEmptyName request) {
        return new AcceptedName(request.name());
    }

    @PostMapping("/not-blank")
    public AcceptedName notBlank(@Valid @RequestBody NotBlankName request) {
        return new AcceptedName(request.name());
    }

    @PostMapping("/required-name")
    public AcceptedName requiredName(@Valid @RequestBody RequiredNameRequest request) {
        return new AcceptedName(request.name());
    }

    public record NotNullName(
            @NotNull(message = "氏名を入力してください") String name) {}

    public record NotEmptyName(
            @NotEmpty(message = "氏名を入力してください") String name) {}

    public record NotBlankName(
            @NotBlank(message = "氏名を入力してください") String name) {}

    public record RequiredNameRequest(@RequiredName String name) {}

    public record AcceptedName(String name) {}
}
