import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public final class JSESSIONID extends Cookie {
    private JSESSIONID() {
        super("JSESSIONID", "");
        setPath("/");
        setSecure(true);
        setHttpOnly(true);
        setMaxAge(0);
    }

    public static JSESSIONID deleteCookie() {
        return new JSESSIONID();
    }

    public static void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.addCookie(deleteCookie());
    }
}
