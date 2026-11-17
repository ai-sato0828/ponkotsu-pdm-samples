package info.syachiku.samples.stringutils;

import org.apache.commons.lang3.StringUtils;

public final class InputPolicy {
    private InputPolicy() { }

    public static boolean isDisplayNameMissing(String name) {
        return StringUtils.isBlank(name);
    }

    public static String memoOrDefault(String memo) {
        return StringUtils.isEmpty(memo) ? "(未設定)" : memo;
    }
}
