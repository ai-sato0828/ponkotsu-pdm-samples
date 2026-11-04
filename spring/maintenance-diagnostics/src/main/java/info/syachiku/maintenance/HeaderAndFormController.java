package info.syachiku.maintenance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
class HeaderAndFormController {
    @GetMapping("/userinfo")
    ResponseEntity<Void> userInfo(@RequestHeader("Authorization") @NotBlank String authorization) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/authorize")
    ResponseEntity<Void> authorize(@Valid @ModelAttribute AuthorizationInput input) {
        return ResponseEntity.noContent().build();
    }
}

record AuthorizationInput(@BindParam("response_type") @NotBlank String responseType) {
}
