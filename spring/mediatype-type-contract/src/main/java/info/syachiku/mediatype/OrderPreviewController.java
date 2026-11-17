package info.syachiku.mediatype;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderPreviewController {
    public record PreviewRequest(String itemCode, int quantity) {}
    public record Preview(String itemCode, int quantity, String status) {}

    @PostMapping(path = "/order-previews",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Preview> preview(@RequestBody PreviewRequest request) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Preview(request.itemCode(), request.quantity(), "preview"));
    }

    @GetMapping(path = "/order-previews/example",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Preview> example() {
        return new ResponseEntity<>(new Preview("BOOK-001", 1, "preview"),
                JsonHeaders.create(), HttpStatus.OK);
    }
}
