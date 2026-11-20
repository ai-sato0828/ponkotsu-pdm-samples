package info.syachiku.jsondate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.OptBoolean;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderDateController {
    static final String RECEIVED_ORDER_DATE = "receivedOrderDate";

    @GetMapping
    public ParsedDate getByDate(
            @RequestParam("orderDate") @DateTimeFormat(pattern = "yyyy/MM/dd") LocalDate orderDate,
            HttpServletRequest request) {
        return accept(orderDate, request);
    }

    @PostMapping("/iso")
    public ParsedDate postIso(@RequestBody MvcDateBody body, HttpServletRequest request) {
        return accept(body.orderDate(), request);
    }

    @PostMapping("/slash")
    public ParsedDate postSlash(@RequestBody SlashDateBody body, HttpServletRequest request) {
        return accept(body.orderDate(), request);
    }

    private ParsedDate accept(LocalDate orderDate, HttpServletRequest request) {
        // 検証用：Controllerに届いた値を、このリクエストの中だけに記録する。
        request.setAttribute(RECEIVED_ORDER_DATE, orderDate);
        return new ParsedDate(orderDate);
    }

    public record MvcDateBody(@DateTimeFormat(pattern = "yyyy/MM/dd") LocalDate orderDate) {}

    public record SlashDateBody(
            @JsonFormat(pattern = "uuuu/MM/dd", lenient = OptBoolean.FALSE) LocalDate orderDate) {}

    public record ParsedDate(LocalDate orderDate) {}
}
