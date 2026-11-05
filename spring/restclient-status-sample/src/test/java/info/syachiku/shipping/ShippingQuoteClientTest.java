package info.syachiku.shipping;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ShippingQuoteClientTest {
    private static final String BASE_URL = "https://shipping.example.test";
    private static final String POSTAL_CODE = "1000001";

    private MockRestServiceServer server;
    private ShippingQuoteClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ShippingQuoteClient(builder, BASE_URL);
    }

    @AfterEach
    void verifyRequest() {
        server.verify();
    }

    @Test
    void returnsQuoteFor200Json() {
        expectQuoteRequest().andRespond(withSuccess(
                """
                {"service":"STANDARD","feeYen":600}
                """, MediaType.APPLICATION_JSON));

        assertEquals(Optional.of(new ShippingQuote("STANDARD", 600)),
                client.findQuote(POSTAL_CODE));
    }

    @Test
    void returnsEmptyFor404() {
        expectQuoteRequest().andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertEquals(Optional.empty(), client.findQuote(POSTAL_CODE));
    }

    @Test
    void preserves503AsFailure() {
        expectQuoteRequest().andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        var error = assertThrows(HttpServerErrorException.ServiceUnavailable.class,
                () -> client.findQuote(POSTAL_CODE));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }

    @Test
    void preserves401AsFailure() {
        expectQuoteRequest().andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        var error = assertThrows(HttpClientErrorException.Unauthorized.class,
                () -> client.findQuote(POSTAL_CODE));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void failsWhen200HasNoQuoteBody() {
        expectQuoteRequest().andRespond(withStatus(HttpStatus.OK));

        var error = assertThrows(IllegalStateException.class,
                () -> client.findQuote(POSTAL_CODE));
        assertEquals("送料見積APIの成功応答に見積がありません", error.getMessage());
    }

    private ResponseActions expectQuoteRequest() {
        return server.expect(requestTo(BASE_URL + "/shipping-quotes/" + POSTAL_CODE))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON_VALUE));
    }
}
