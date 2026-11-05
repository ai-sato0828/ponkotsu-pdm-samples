package info.syachiku.giftwrap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GiftWrapFeeSpec {
    private final GiftWrapFee fee = new GiftWrapFee();

    @Test
    void chargesFeeBelowThreshold() {
        assertEquals(200, fee.calculate(2999));
    }

    @Test
    void waivesFeeAtThreshold() {
        assertEquals(0, fee.calculate(3000));
    }
}
