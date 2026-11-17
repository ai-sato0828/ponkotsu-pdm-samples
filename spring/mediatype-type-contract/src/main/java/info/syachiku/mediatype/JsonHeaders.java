package info.syachiku.mediatype;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public final class JsonHeaders {
    private JsonHeaders() {}

    public static HttpHeaders create() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
