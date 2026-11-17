package info.syachiku.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Page<Inquiry> findByStatus(Status status, Pageable pageable);
}
