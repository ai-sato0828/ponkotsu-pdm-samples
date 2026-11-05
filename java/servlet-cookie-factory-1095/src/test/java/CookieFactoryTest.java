import static org.junit.jupiter.api.Assertions.*;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

class CookieFactoryTest {
    @Test
    void oldFactoryReturnsTheExpectedExpirationCookie() {
        assertExpirationCookie(new CookieLogic().deleteJsessionCookie());
    }

    @Test
    void namedFactoryReturnsTheSameContractWithItsOwnType() {
        JSESSIONID cookie = JSESSIONID.deleteCookie();
        assertExpirationCookie(cookie);
    }

    @Test
    void factoryDoesNotReuseAMutableCookieBetweenResponses() {
        JSESSIONID first = JSESSIONID.deleteCookie();
        first.setPath("/changed-by-one-response");
        JSESSIONID second = JSESSIONID.deleteCookie();
        assertNotSame(first, second);
        assertExpirationCookie(second);
    }

    private static void assertExpirationCookie(Cookie cookie) {
        assertAll(
            () -> assertEquals("JSESSIONID", cookie.getName()),
            () -> assertEquals("", cookie.getValue()),
            () -> assertEquals("/", cookie.getPath()),
            () -> assertNull(cookie.getDomain()),
            () -> assertEquals(0, cookie.getMaxAge()),
            () -> assertTrue(cookie.getSecure()),
            () -> assertTrue(cookie.isHttpOnly())
        );
    }
}
