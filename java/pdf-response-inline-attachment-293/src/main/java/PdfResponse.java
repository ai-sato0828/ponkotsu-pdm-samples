import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;

public final class PdfResponse {
    private PdfResponse() {}

    public static void send(HttpServletResponse response, byte[] pdf, boolean preview)
            throws IOException {
        String disposition = preview ? "inline" : "attachment";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", disposition + "; filename=\"sample.pdf\"");
        response.getOutputStream().write(pdf);
    }
}
