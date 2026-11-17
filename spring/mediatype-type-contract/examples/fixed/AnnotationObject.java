import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

class AnnotationObject {
    @GetMapping(path = "/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    String preview() { return "{}"; }
}
