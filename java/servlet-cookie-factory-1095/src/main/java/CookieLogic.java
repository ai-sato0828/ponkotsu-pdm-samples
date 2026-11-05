import jakarta.servlet.http.Cookie;

public class CookieLogic {
    public Cookie deleteJsessionCookie() {
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        return cookie;
    }
}
