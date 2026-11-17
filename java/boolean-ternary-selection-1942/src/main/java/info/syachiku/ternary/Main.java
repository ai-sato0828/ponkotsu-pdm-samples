package info.syachiku.ternary;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        for (boolean useExisting : new boolean[]{true, false}) {
            for (Boolean configured : new Boolean[]{null, Boolean.TRUE, Boolean.FALSE}) {
                String broken;
                try {
                    broken = String.valueOf(TernarySelection.broken(useExisting, configured));
                } catch (NullPointerException failure) {
                    broken = failure.getClass().getSimpleName();
                }
                Boolean fixed = TernarySelection.preserveNullable(useExisting, configured);
                System.out.printf("CASE useExisting=%s configured=%s broken=%s fixed=%s%n",
                        useExisting, configured, broken, fixed);
            }
        }
        Boolean selected = TernarySelection.preserveNullable(true, null);
        String ifResult;
        try {
            if (selected) {
                ifResult = "true";
            } else {
                ifResult = "false";
            }
        } catch (NullPointerException failure) {
            ifResult = failure.getClass().getSimpleName();
        }
        System.out.printf("BOUNDARY ifSelected=%s trueOnly=%s%n",
                ifResult, Boolean.TRUE.equals(selected));
    }
}
