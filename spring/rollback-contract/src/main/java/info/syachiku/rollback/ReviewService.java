package info.syachiku.rollback;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final RegistrationStore store;

    ReviewService(RegistrationStore store) {
        this.store = store;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void reviewInSameTransaction(String requestId, boolean failReview) {
        writeReview(requestId, failReview);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reviewInOwnTransaction(String requestId, boolean failReview) {
        writeReview(requestId, failReview);
    }

    private void writeReview(String requestId, boolean failReview) {
        store.insertReview(requestId);
        if (failReview) {
            throw new ReviewFailedException("審査処理に失敗");
        }
    }
}
