package info.syachiku.audit;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

record InquiryRow(Long id, String title) {}
record InquiryPage(List<InquiryRow> items, int page, int size,
                   long totalElements, int totalPages, boolean hasNext) {}

@RestController
class InquiryController {
    private final InquiryRepository repository;

    InquiryController(InquiryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/inquiries")
    InquiryPage list(@RequestParam(defaultValue = "0") int page,
                     @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "pageまたはsizeが範囲外です");
        }
        Sort order = Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id"));
        var result = repository.findByStatus(Status.OPEN,
                PageRequest.of(page, size, order));
        var rows = result.getContent().stream()
                .map(i -> new InquiryRow(i.getId(), i.getTitle()))
                .toList();
        return new InquiryPage(rows, page, size, result.getTotalElements(),
                result.getTotalPages(), result.hasNext());
    }
}
