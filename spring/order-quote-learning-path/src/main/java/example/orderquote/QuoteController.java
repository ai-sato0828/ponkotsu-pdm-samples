package example.orderquote;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuoteController {
    private final QuoteCalculator calculator;

    public QuoteController(QuoteCalculator calculator) {
        this.calculator = calculator;
    }

    @PostMapping("/order-quotes")
    public QuoteResponse quote(@RequestBody QuoteRequest request) {
        if (request.lines() == null) {
            throw new InvalidBodyException("linesが必要です");
        }

        List<LineItem> lines = new ArrayList<>();
        for (LineRequest line : request.lines()) {
            if (line == null || line.quantity() == null || line.unitPrice() == null) {
                throw new InvalidBodyException("各明細にquantityとunitPriceが必要です");
            }
            lines.add(new LineItem(line.quantity(), line.unitPrice()));
        }
        return new QuoteResponse(calculator.total(lines));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidBodyException.class)
    public QuoteError invalidBody(InvalidBodyException error) {
        return new QuoteError("INVALID_BODY", error.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidQuoteException.class)
    public QuoteError invalidQuote(InvalidQuoteException error) {
        return new QuoteError("INVALID_QUOTE", error.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public QuoteError unreadableJson() {
        return new QuoteError("INVALID_JSON", "本文を整数の明細を持つJSONとして読み取れません");
    }

    public record QuoteRequest(List<LineRequest> lines) {}

    public record LineRequest(Integer quantity, Integer unitPrice) {}

    public record QuoteResponse(long total) {}

    public record QuoteError(String code, String message) {}

    private static final class InvalidBodyException extends RuntimeException {
        InvalidBodyException(String message) {
            super(message);
        }
    }
}
