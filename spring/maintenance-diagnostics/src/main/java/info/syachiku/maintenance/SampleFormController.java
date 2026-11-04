package info.syachiku.maintenance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
class SampleFormController {
    @PostMapping("/sample")
    ResponseEntity<SampleForm> submit(@Valid @ModelAttribute SampleForm form, BindingResult errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(form);
    }
}

record SampleForm(@NotBlank String userid, @NotBlank String username,
                  @NotBlank String empno, List<String> roles) {
}
