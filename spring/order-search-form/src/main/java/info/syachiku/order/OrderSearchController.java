package info.syachiku.order;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class OrderSearchController {
    private final OrderSearchService orderService;

    OrderSearchController(OrderSearchService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/demo/orders/simple")
    @ResponseBody
    public List<Order> simple(@RequestParam(name = "keyword", required = false) String keyword,
                              @RequestParam(name = "status", required = false) String status) {
        return orderService.search(new OrderSearchCondition(keyword, status, null, null, null, 1));
    }

    @GetMapping("/demo/orders/before")
    @ResponseBody
    public List<Order> before(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate toDate,
            @RequestParam(name = "staffId", required = false) String staffId,
            @RequestParam(name = "page", defaultValue = "1") int page) {
        return orderService.search(new OrderSearchCondition(keyword, status, fromDate, toDate, staffId, page));
    }

    @GetMapping("/orders")
    public String list(@Valid @ModelAttribute("searchForm") OrderSearchForm form,
                       BindingResult errors, Model model) {
        if (!errors.hasFieldErrors("fromDate") && !errors.hasFieldErrors("toDate")
                && form.getFromDate() != null && form.getToDate() != null
                && form.getFromDate().isAfter(form.getToDate())) {
            errors.rejectValue("toDate", "date.range", "終了日は開始日以降にしてください");
        }
        if (errors.hasErrors()) {
            return "orders/list";
        }
        model.addAttribute("orders", orderService.search(form.toCondition()));
        return "orders/list";
    }
}
