package info.syachiku.controller404;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void clearObservation() {
        userService.resetCalls();
    }

    @ParameterizedTest(name = "{0}: {1} {2} -> {3}, service={4}")
    @CsvSource({
        "H01, GET,  /sales/api/users/10,  200, 1",
        "H02, GET,  /sales/api/user/10,   404, 0",
        "H03, GET,  /sales/api/users,     404, 0",
        "H04, POST, /sales/api/users/10,  405, 0",
        "H05, GET,  /sales/api/users/abc, 400, 0",
        "H06, GET,  /api/users/10,        404, 0",
        "H07, GET,  /sales/api/users/999, 404, 1"
    })
    void actualHttpDistinguishesRouteMethodInputAndMissingData(
            String caseId, String method, String path, int expectedStatus, int expectedCalls)
            throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Accept", "application/json")
                .method(method, HttpRequest.BodyPublishers.noBody()).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.printf("%s %s %s status=%d service=%d%n",
                caseId, method, path, response.statusCode(), userService.calls());

        assertEquals(expectedStatus, response.statusCode(), caseId);
        assertEquals(expectedCalls, userService.calls(), caseId);
        if (expectedStatus == 200) {
            var json = objectMapper.readTree(response.body());
            assertEquals(10L, json.get("id").longValue());
            assertEquals("Tanaka", json.get("name").textValue());
        }
        if (expectedStatus == 405) {
            assertTrue(response.headers().firstValue("Allow").orElse("").contains("GET"));
        }
    }

    @Test
    void mockMvcCallsRegisteredControllerWithExplicitContextPath() throws Exception {
        mockMvc.perform(get("/sales/api/users/{id}", 10L).contextPath("/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Tanaka"));
        assertEquals(1, userService.calls());
    }

    @Test
    void mockMvcWrongUrlDoesNotCallService() throws Exception {
        mockMvc.perform(get("/sales/api/user/{id}", 10L).contextPath("/sales"))
                .andExpect(status().isNotFound());
        assertEquals(0, userService.calls());
    }
}
