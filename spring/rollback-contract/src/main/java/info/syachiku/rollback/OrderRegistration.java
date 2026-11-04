package info.syachiku.rollback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class OrderRegistration {
    private static final Logger LOG = LoggerFactory.getLogger(OrderRegistration.class);

    private final RegistrationStore store;
    private final ReviewService reviews;

    OrderRegistration(RegistrationStore store, ReviewService reviews) {
        this.store = store;
        this.reviews = reviews;
    }

    @Transactional
    public String registerAndCatch(String requestId, boolean failReview) {
        store.insertOrder(requestId);
        try {
            reviews.reviewInSameTransaction(requestId, failReview);
        } catch (ReviewFailedException failure) {
            boolean rollbackOnly = TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
            LOG.info("outer caught review failure; rollbackOnly={}", rollbackOnly);
        }
        store.updateOrder(requestId, "ACCEPTED");
        LOG.info("outer method reached its return");
        return "ACCEPTED";
    }

    @Transactional
    public void registerWithRequiredReview(String requestId, boolean failReview) {
        store.insertOrder(requestId);
        reviews.reviewInSameTransaction(requestId, failReview);
        store.updateOrder(requestId, "REVIEWED");
    }

    @Transactional
    public void registerWithIndependentReview(
            String requestId, boolean failReview, boolean failOrder) {
        store.insertOrder(requestId);
        try {
            reviews.reviewInOwnTransaction(requestId, failReview);
            store.updateOrder(requestId, "REVIEWED");
        } catch (ReviewFailedException failure) {
            // The contract allows the order to stay PENDING for later review.
            LOG.info("independent review failed; order stays PENDING");
        }
        if (failOrder) {
            throw new IllegalStateException("注文登録の後続処理に失敗");
        }
    }
}
