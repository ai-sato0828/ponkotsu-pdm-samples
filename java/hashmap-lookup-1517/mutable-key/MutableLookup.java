import java.util.*;

public class MutableLookup {
    public static void main(String[] args) {
        UserKey key = new UserKey("U001");
        Map<UserKey, String> names = new HashMap<>();

        names.put(key, "田中");

        key.setValue("U002");

        System.out.println(names.get(key)); // 取れないことがある
    }
}

final class UserKey {
    private String value;
    UserKey(String value) { this.value = value; }
    void setValue(String value) { this.value = value; }
    @Override public boolean equals(Object obj) {
        return obj instanceof UserKey other && value.equals(other.value);
    }
    @Override public int hashCode() { return value.hashCode(); }
}
