package info.syachiku.characterization;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeliveryDecisionTest {
    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("sameBehaviorCases")
    void beforeAndAfterGiveTheSameResult(
            String version, String caseId, DeliveryDecision decision,
            boolean prohibited, String postalCode, String zone, String expected) {
        assertEquals(expected, decision.check(prohibited, postalCode, zone));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("remoteReviewCases")
    void remoteReviewChangesOnlyTheApprovedCase(
            String caseId, boolean prohibited, String postalCode, String zone, String expected) {
        var decision = RefactoredDeliveryDecision.withRemoteReview();

        assertEquals(expected, decision.check(prohibited, postalCode, zone));
    }

    static Stream<Arguments> sameBehaviorCases() {
        return Stream.concat(
                cases(false).map(c -> c.forImplementation("Before", new LegacyDeliveryDecision())),
                cases(false).map(c -> c.forImplementation("After", new RefactoredDeliveryDecision())));
    }

    static Stream<Arguments> remoteReviewCases() {
        return cases(true).map(c -> Arguments.of(c.id(), c.prohibited(), c.postalCode(), c.zone(), c.expected()));
    }

    private static Stream<DeliveryCase> cases(boolean holdRemoteZone) {
        return Stream.of(
                new DeliveryCase("C01", false, "100-0001", "NORMAL", "OK"),
                new DeliveryCase("C02", false, " ", "NORMAL", "HOLD"),
                new DeliveryCase("C03", false, null, "NORMAL", "HOLD"),
                new DeliveryCase("C04", true, "100-0001", "NORMAL", "NG"),
                new DeliveryCase("C05", true, " ", "NORMAL", "NG"),
                new DeliveryCase("C06", false, "100-0001", "REMOTE", holdRemoteZone ? "HOLD" : "OK"),
                new DeliveryCase("C07", true, "100-0001", "REMOTE", "NG"));
    }

    private record DeliveryCase(
            String id, boolean prohibited, String postalCode, String zone, String expected) {
        Arguments forImplementation(String version, DeliveryDecision decision) {
            return Arguments.of(version, id, decision, prohibited, postalCode, zone, expected);
        }
    }
}
