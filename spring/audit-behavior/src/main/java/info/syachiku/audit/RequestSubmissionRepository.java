package info.syachiku.audit;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface RequestSubmissionRepository extends JpaRepository<RequestSubmission, Long> {
    Optional<RequestSubmission> findByRequestKey(String requestKey);
}
