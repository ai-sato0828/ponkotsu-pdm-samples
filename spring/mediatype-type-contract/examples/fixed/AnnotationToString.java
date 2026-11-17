import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

class AnnotationToString {
    @GetMapping(path = "/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    String preview() { return "{}"; }
}
