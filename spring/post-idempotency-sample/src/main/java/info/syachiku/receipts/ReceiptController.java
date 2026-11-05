package info.syachiku.receipts;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ReceiptController {
    private final ReceiptService service;

    public ReceiptController(ReceiptService service) {
        this.service = service;
    }

    @PostMapping("/applications")
    public ResponseEntity<ReceiptService.Receipt> register(@Valid @RequestBody ApplicationRequest request) {
        try {
            var result = service.register(request.requestId(), request.title());
            var status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
            return ResponseEntity.status(status).body(result.receipt());
        } catch (ReceiptService.RequestIdConflict ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "受付IDが別の申請に使われています");
        }
    }

    public record ApplicationRequest(@NotNull UUID requestId, @NotBlank @Size(max = 120) String title) {
    }
}
