package info.syachiku.paging;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Profile("strict")
class StrictInquiryController {
    private final InquirySearch search;

    StrictInquiryController(InquirySearch search) {
        this.search = search;
    }

    @GetMapping("/inquiries")
    PageSelection list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 50
                || (long) page * size > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "pageまたはsizeが範囲外です");
        }
        return search.run(PageRequest.of(page, size));
    }
}

@RestController
@Profile("!strict")
class ResolvedInquiryController {
    private final InquirySearch search;

    ResolvedInquiryController(InquirySearch search) {
        this.search = search;
    }

    @GetMapping("/inquiries")
    PageSelection list(Pageable pageable) {
        return search.run(pageable);
    }
}
