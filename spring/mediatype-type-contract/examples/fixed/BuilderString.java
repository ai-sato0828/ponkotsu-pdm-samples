import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class BuilderString {
    ResponseEntity<String> preview() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{}");
    }
}
