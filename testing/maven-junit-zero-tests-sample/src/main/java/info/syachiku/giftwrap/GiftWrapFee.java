package info.syachiku.giftwrap;

public final class GiftWrapFee {
    public int calculate(int subtotalYen) {
        return subtotalYen >= 3000 ? 0 : 200;
    }
}
