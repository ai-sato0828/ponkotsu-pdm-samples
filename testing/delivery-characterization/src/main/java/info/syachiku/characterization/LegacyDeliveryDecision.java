package info.syachiku.characterization;

public final class LegacyDeliveryDecision implements DeliveryDecision {
    @Override
    public String check(boolean prohibited, String postalCode, String zone) {
        if (prohibited) {
            return "NG";
        }
        if (postalCode == null || postalCode.isBlank()) {
            return "HOLD";
        }
        return "OK";
    }
}
