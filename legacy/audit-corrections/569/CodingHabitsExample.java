import java.util.Objects;

public class CodingHabitsExample {
    private static final String ACTIVE = "ACTIVE";
    private static final int EXPORT_COLUMN_COUNT = 3;

    enum RegistrationResult {
        REGISTERED, ALREADY_REGISTERED
    }

    static boolean isActive(String status) {
        return ACTIVE.equals(status);
    }

    static String requireStatus(String status) {
        return Objects.requireNonNull(status, "status");
    }

    static RegistrationResult register(boolean alreadyRegistered) {
        return alreadyRegistered
                ? RegistrationResult.ALREADY_REGISTERED
                : RegistrationResult.REGISTERED;
    }

    static String csvHeader() {
        String[] columns = new String[EXPORT_COLUMN_COUNT];
        columns[0] = "user_id";
        columns[1] = "name";
        columns[2] = "status";
        // 連携先は、CSVの列名と並び順の両方で値を読み取る。
        return String.join(",", columns);
    }

    public static void main(String[] args) {
        check(isActive("ACTIVE"));
        check(!isActive("DISABLED"));
        check(!isActive(null));

        try {
            requireStatus(null);
            throw new AssertionError("missing required status accepted");
        } catch (NullPointerException e) {
            check(e.getMessage().equals("status"));
        }

        check(register(false) == RegistrationResult.REGISTERED);
        check(register(true) == RegistrationResult.ALREADY_REGISTERED);
        check(csvHeader().equals("user_id,name,status"));
        System.out.println("7 checks passed");
    }

    private static void check(boolean condition) {
        if (!condition) {
            throw new AssertionError();
        }
    }
}
