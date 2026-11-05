package info.syachiku.shipping;

import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

public final class ShippingQuoteClient {
    private final RestClient client;

    public ShippingQuoteClient(RestClient.Builder builder, String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public Optional<ShippingQuote> findQuote(String postalCode) {
        try {
            ShippingQuote quote = client.get()
                    .uri("/shipping-quotes/{postalCode}", postalCode)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ShippingQuote.class);
            if (quote == null) {
                throw new IllegalStateException("送料見積APIの成功応答に見積がありません");
            }
            return Optional.of(quote);
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        }
    }
}
