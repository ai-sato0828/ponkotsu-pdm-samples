package info.syachiku.paging;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

record PageSelection(int page, int size) {}

@Service
class InquirySearch {
    PageSelection run(Pageable pageable) {
        return new PageSelection(pageable.getPageNumber(), pageable.getPageSize());
    }
}
