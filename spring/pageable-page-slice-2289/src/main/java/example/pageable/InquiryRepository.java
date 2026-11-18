package example.pageable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Page<Inquiry> findByStatus(Inquiry.Status status, Pageable pageable);

    Slice<Inquiry> findSliceByStatus(Inquiry.Status status, Pageable pageable);
}
