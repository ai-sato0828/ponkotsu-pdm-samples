package info.syachiku.order500;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RequestIdFilterTest {
    @Test
    void F01_sequentialRequestsOnSameThread_haveSeparateIdsAndClearMdc() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        assertNull(MDC.get("requestId"));
        MockHttpServletRequest first = new MockHttpServletRequest();
        first.addHeader("X-Request-Id", "filter-first");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(first, firstResponse, (request, response) -> {
            assertEquals("filter-first", MDC.get("requestId"));
            assertEquals("filter-first", request.getAttribute(RequestIdFilter.ATTRIBUTE));
        });
        assertEquals("filter-first", firstResponse.getHeader("X-Request-Id"));
        assertNull(MDC.get("requestId"));

        MockHttpServletRequest second = new MockHttpServletRequest();
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(second, secondResponse, (request, response) -> {
            String id = (String) request.getAttribute(RequestIdFilter.ATTRIBUTE);
            assertDoesNotThrow(() -> UUID.fromString(id));
            assertNotEquals("filter-first", id);
            assertEquals(id, MDC.get("requestId"));
        });
        assertNotEquals(firstResponse.getHeader("X-Request-Id"), secondResponse.getHeader("X-Request-Id"));
        assertNull(MDC.get("requestId"));
    }

    @Test
    void F02_chainIOException_stillClearsMdcInFinally() {
        RequestIdFilter filter = new RequestIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "filter-throws");
        IOException failure = new IOException("local test failure");
        IOException actual = assertThrows(IOException.class, () -> filter.doFilter(request,
                new MockHttpServletResponse(), (req, res) -> {
                    assertEquals("filter-throws", MDC.get("requestId"));
                    throw failure;
                }));
        assertSame(failure, actual);
        assertNull(MDC.get("requestId"));
    }
}
