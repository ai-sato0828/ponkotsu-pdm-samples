package info.syachiku.order500;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
class OrderHttpTest {
    @LocalServerPort
    private int port;
    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }

    private HttpResponse<String> post(String boundary, String requestId, String body) throws Exception {
        return http.send(HttpRequest.newBuilder(uri("/api/orders/" + boundary))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("X-Request-Id", requestId)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode readback(String sourceRequestId) throws Exception {
        // POST完了後の別HTTP GET。テスト全体へ@Transactionalを付けない。
        HttpResponse<String> response = http.send(HttpRequest.newBuilder(
                        uri("/api/orders/by-request/" + sourceRequestId))
                .timeout(Duration.ofSeconds(10)).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonNode body = json.readTree(response.body());
        assertEquals(sourceRequestId, body.get("sourceRequestId").asText());
        return body;
    }

    private void assertError(HttpResponse<String> response, int expectedStatus, String requestId)
            throws Exception {
        assertEquals(expectedStatus, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        assertEquals(requestId, response.headers().firstValue("X-Request-Id").orElseThrow());
        JsonNode body = json.readTree(response.body());
        assertEquals(4, body.size());
        assertEquals(expectedStatus, body.get("status").asInt());
        assertEquals(requestId, body.get("requestId").asText());
        assertEquals(expectedStatus == 500 ? "INTERNAL_ERROR" : "INVALID_REQUEST",
                body.get("code").asText());
        assertEquals(expectedStatus == 500 ? "処理を完了できませんでした" : "入力内容を確認してください",
                body.get("message").asText());
        assertFalse(response.body().contains("SocketTimeoutException"));
        assertFalse(response.body().contains(".java:"));
        assertFalse(response.body().contains("INSERT INTO"));
    }

    @Test
    void O00_initialUnsubmittedRequest_hasNoRows() throws Exception {
        JsonNode db = readback("http-never-submitted");
        assertEquals(0, db.get("count").asInt());
        assertEquals(0, db.get("orders").size());
    }

    @Test
    void O01_success_returns201AndOneCommittedOrder(CapturedOutput output) throws Exception {
        String id = "http-individual-ok";
        HttpResponse<String> response = post("individual", id,
                "{\"item\":\"BOOK\",\"failNotification\":false}");
        assertEquals(201, response.statusCode());
        JsonNode created = json.readTree(response.body());
        assertEquals(id, created.get("requestId").asText());
        assertEquals(id, response.headers().firstValue("X-Request-Id").orElseThrow());
        JsonNode db = readback(id);
        assertEquals(1, db.get("count").asInt());
        assertEquals(created, db.get("orders").get(0));
        assertTrue(output.getAll().contains("operation=createOrder result=success requestId=" + id));
    }

    @Test
    void O02_individualFailure_returns500ButKeepsOneOrder(CapturedOutput output) throws Exception {
        String id = "http-individual-fail";
        HttpResponse<String> response = post("individual", id,
                "{\"item\":\"BOOK\",\"failNotification\":true}");
        assertError(response, 500, id);
        JsonNode db = readback(id);
        assertEquals(1, db.get("count").asInt());
        assertEquals("BOOK", db.get("orders").get(0).get("item").asText());
        assertEquals(id, db.get("orders").get(0).get("requestId").asText());
        assertTrue(output.getAll().contains("path=/api/orders/individual requestId=" + id));
        assertTrue(output.getAll().contains("Caused by: java.net.SocketTimeoutException: simulated notification timeout"));
        assertTrue(output.getAll().contains("NotificationStub.send(NotificationStub.java:"));
        assertTrue(output.getAll().contains("IndividualCommitOrderService.create(IndividualCommitOrderService.java:"));
    }

    @Test
    void O03_serviceTransactionFailure_returns500AndRollsBack(CapturedOutput output) throws Exception {
        String id = "http-transactional-fail";
        HttpResponse<String> response = post("transactional", id,
                "{\"item\":\"BOOK\",\"failNotification\":true}");
        assertError(response, 500, id);
        JsonNode db = readback(id);
        assertEquals(0, db.get("count").asInt());
        assertEquals(0, db.get("orders").size());
        assertTrue(output.getAll().contains("path=/api/orders/transactional requestId=" + id));
        assertTrue(output.getAll().contains("commitNotYet=true requestId=" + id));
        assertTrue(output.getAll().contains("Caused by: java.net.SocketTimeoutException: simulated notification timeout"));
        assertTrue(output.getAll().contains("TransactionalOrderService.create(TransactionalOrderService.java:"));
    }

    @Test
    void O04_serviceTransactionSuccess_returns201AndOneOrder() throws Exception {
        String id = "http-transactional-ok";
        HttpResponse<String> response = post("transactional", id,
                "{\"item\":\"BOOK\",\"failNotification\":false}");
        assertEquals(201, response.statusCode());
        JsonNode db = readback(id);
        assertEquals(1, db.get("count").asInt());
        assertEquals(json.readTree(response.body()), db.get("orders").get(0));
    }

    @Test
    void O05_malformedJson_returnsSafe400AndNoRows() throws Exception {
        String id = "http-malformed";
        assertError(post("individual", id, "{"), 400, id);
        assertEquals(0, readback(id).get("count").asInt());
    }

    @Test
    void O06_missingItem_returnsSafe400AndNoRows() throws Exception {
        String id = "http-missing-item";
        assertError(post("individual", id,
                "{}"), 400, id);
        assertEquals(0, readback(id).get("count").asInt());
    }

    @Test
    void O07_invalidSuppliedRequestId_isReplacedWithUuid() throws Exception {
        HttpResponse<String> response = post("individual", "bad id",
                "{\"item\":\"BOOK\",\"failNotification\":false}");
        assertEquals(201, response.statusCode());
        String id = response.headers().firstValue("X-Request-Id").orElseThrow();
        assertDoesNotThrow(() -> UUID.fromString(id));
        assertEquals(id, json.readTree(response.body()).get("requestId").asText());
        assertEquals(1, readback(id).get("count").asInt());
    }

    @Test
    void O08_wrongMethod_preserves405AndAllow() throws Exception {
        String id = "http-wrong-method";
        HttpResponse<String> response = http.send(HttpRequest.newBuilder(uri("/api/orders/individual"))
                .header("X-Request-Id", id).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertError(response, 405, id);
        assertTrue(response.headers().firstValue("Allow").orElse("").contains("POST"));
        assertEquals(0, readback(id).get("count").asInt());
    }

    @Test
    void O09_wrongMediaType_preserves415() throws Exception {
        String id = "http-wrong-media";
        HttpResponse<String> response = http.send(HttpRequest.newBuilder(uri("/api/orders/individual"))
                .header("X-Request-Id", id).header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString("BOOK")).build(),
                HttpResponse.BodyHandlers.ofString());
        assertError(response, 415, id);
        assertEquals(0, readback(id).get("count").asInt());
    }
}
