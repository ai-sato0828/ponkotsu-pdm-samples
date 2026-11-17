package info.syachiku.ternary;

public final class TernarySelection {
    private TernarySelection() {}

    // Intentional failure: the conditional expression has primitive boolean type.
    public static Boolean broken(boolean useExisting, Boolean configured) {
        Boolean selected = useExisting ? configured : false;
        return selected;
    }

    // Contract: keep true, false, or null only when useExisting is true.
    public static Boolean preserveNullable(boolean useExisting, Boolean configured) {
        Boolean selected = useExisting ? configured : Boolean.FALSE;
        return selected;
    }
}
