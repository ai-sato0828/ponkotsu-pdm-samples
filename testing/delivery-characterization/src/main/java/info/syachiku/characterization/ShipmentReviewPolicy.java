package info.syachiku.characterization;

final class ShipmentReviewPolicy {
    private final boolean holdRemoteZone;

    ShipmentReviewPolicy(boolean holdRemoteZone) {
        this.holdRemoteZone = holdRemoteZone;
    }

    boolean requiresReview(String postalCode, String zone) {
        return postalCode == null || postalCode.isBlank()
                || (holdRemoteZone && "REMOTE".equals(zone));
    }
}
