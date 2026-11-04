package info.syachiku.characterization;

public interface DeliveryDecision {
    String check(boolean prohibited, String postalCode, String zone);
}
