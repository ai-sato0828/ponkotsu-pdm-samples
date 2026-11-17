package info.syachiku.tomapnull;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class Main {
    private Main() {
    }

    // BEGIN reproduction
    public static void main(String[] args) {
        List<User> users = List.of(
                new User("U001", "田中"),
                new User("U002", null));

        printFailure("twoArguments", () -> UserMaps.toMapTwoArguments(users));
        printFailure("hashMapSupplier", () -> UserMaps.toMapWithHashMapSupplier(users));

        System.out.println("filter=" + new TreeMap<>(UserMaps.withoutUnsetNames(users)));
        System.out.println("default=" + new TreeMap<>(UserMaps.withDefaultName(users, "未設定")));
        Map<String, String> preserved = UserMaps.preservingUnsetNames(users);
        System.out.println("preserve=" + new TreeMap<>(preserved));
        System.out.println("U002 present=" + preserved.containsKey("U002")
                + ", value=" + preserved.get("U002"));
        System.out.println("U999 present=" + preserved.containsKey("U999")
                + ", value=" + preserved.get("U999"));

        printFailure("duplicateFirstNull", () -> UserMaps.preservingUnsetNames(List.of(
                new User("U002", null), new User("U002", "鈴木"))));
    }
    // END reproduction

    private static void printFailure(String label, Runnable action) {
        try {
            action.run();
            throw new AssertionError(label + " should fail");
        } catch (NullPointerException | IllegalArgumentException expected) {
            System.out.println(label + "=" + expected.getClass().getSimpleName());
        }
    }
}
