import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ResourceScenarios {
    private ResourceScenarios() {}

    public enum Scenario {
        NORMAL(false, false, false, false),
        BODY_FAIL(true, false, false, false),
        BODY_CLOSE_FAIL(true, true, true, false),
        CLOSE_FAIL(false, true, true, false),
        ACQUIRE_CLOSE_FAIL(false, true, false, true),
        ACQUIRE_FAIL(false, false, false, true);

        final boolean bodyFails;
        final boolean aCloseFails;
        final boolean bCloseFails;
        final boolean bOpenFails;

        Scenario(boolean bodyFails, boolean aCloseFails,
                 boolean bCloseFails, boolean bOpenFails) {
            this.bodyFails = bodyFails;
            this.aCloseFails = aCloseFails;
            this.bCloseFails = bCloseFails;
            this.bOpenFails = bOpenFails;
        }
    }

    public record Outcome(List<String> trace, IOException failure) {
        public String primaryMessage() {
            return failure == null ? "none" : failure.getMessage();
        }

        public List<String> suppressedMessages() {
            return failure == null ? List.of() : Arrays.stream(failure.getSuppressed())
                    .map(Throwable::getMessage).toList();
        }

        public List<String> closeOrder() {
            return trace.stream().filter(event -> event.startsWith("close:"))
                    .map(event -> event.substring("close:".length())).toList();
        }
    }

    // BEGIN run
    public static Outcome run(Scenario scenario) {
        List<String> trace = new ArrayList<>();
        try (RecordingResource a = RecordingResource.open(
                    "A", false, scenario.aCloseFails, trace);
             RecordingResource b = RecordingResource.open(
                    "B", scenario.bOpenFails, scenario.bCloseFails, trace)) {
            trace.add("body");
            if (scenario.bodyFails) {
                throw new IOException("body");
            }
        } catch (IOException failure) {
            return new Outcome(List.copyOf(trace), failure);
        }
        return new Outcome(List.copyOf(trace), null);
    }
    // END run

    // BEGIN main
    public static void main(String[] args) {
        for (Scenario scenario : Scenario.values()) {
            Outcome outcome = run(scenario);
            System.out.printf("%s main=%s suppressed=%s close=%s trace=%s%n", scenario,
                    outcome.primaryMessage(), outcome.suppressedMessages(), outcome.closeOrder(), outcome.trace());
        }
    }
    // END main
}
