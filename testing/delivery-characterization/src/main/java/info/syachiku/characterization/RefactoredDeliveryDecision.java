package info.syachiku.characterization;

public final class RefactoredDeliveryDecision implements DeliveryDecision {
    private final ShipmentReviewPolicy reviewPolicy;

    public RefactoredDeliveryDecision() {
        this(new ShipmentReviewPolicy(false));
    }

    private RefactoredDeliveryDecision(ShipmentReviewPolicy reviewPolicy) {
        this.reviewPolicy = reviewPolicy;
    }

    public static RefactoredDeliveryDecision withRemoteReview() {
        return new RefactoredDeliveryDecision(new ShipmentReviewPolicy(true));
    }

    @Override
    public String check(boolean prohibited, String postalCode, String zone) {
        if (prohibited) {
            return "NG";
        }
        if (reviewPolicy.requiresReview(postalCode, zone)) {
            return "HOLD";
        }
        return "OK";
    }
}
