import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

class DoubleMeasurementArrayTest {
    // 測定区間の合計長さ（m）を比較する。この値はこの教材の許容幅。
    private static final double DELTA_METRES = 1e-9;

    @Test
    void exactComparisonRejectsAdditionRoundingDifference() {
        double[] expectedMetres = {0.3};
        double[] actualMetres = {0.1 + 0.2};
        assertThrows(AssertionFailedError.class,
                () -> assertArrayEquals(expectedMetres, actualMetres));
        System.out.printf("EXACT expected=%s actual=%s rejected=true%n",
                expectedMetres[0], actualMetres[0]);
    }

    @Test
    void chosenToleranceAcceptsAdditionRoundingDifference() {
        assertArrayEquals(new double[]{0.3}, new double[]{0.1 + 0.2}, DELTA_METRES);
        System.out.printf("ROUNDING deltaMetres=%s accepted=true%n", DELTA_METRES);
    }

    @Test
    void differenceBeyondChosenToleranceIsStillRejected() {
        assertThrows(AssertionFailedError.class,
                () -> assertArrayEquals(new double[]{0.3}, new double[]{0.31}, DELTA_METRES));
        System.out.println("VALUE expectedMetres=0.3 actualMetres=0.31 rejected=true");
    }

    @Test
    void lengthMismatchIsRejectedEvenWithTolerance() {
        double[] expectedMetres = {0.3};
        double[] actualMetres = {0.1 + 0.2, 0.3};
        assertEquals(1, expectedMetres.length);
        assertEquals(2, actualMetres.length);
        assertThrows(AssertionFailedError.class,
                () -> assertArrayEquals(expectedMetres, actualMetres, DELTA_METRES));
        System.out.println("LENGTH expected=1 actual=2 rejected=true");
    }
}
