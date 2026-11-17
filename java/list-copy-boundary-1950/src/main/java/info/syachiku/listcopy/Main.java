package info.syachiku.listcopy;

import java.util.ArrayList;
import java.util.List;

public final class Main {
    private Main() {}

    static List<User> sourceUsers() {
        return new ArrayList<>(List.of(
                new User("U001", "田中", new ArrayList<>(List.of("general"))),
                new User("U002", "鈴木", new ArrayList<>(List.of("support")))));
    }

    public static void main(String[] args) {
        structureDemo();
        elementDemo();
        editingDemo();
        readOnlyDemo();
    }

    // BEGIN structureDemo
    private static void structureDemo() {
        List<User> original = sourceUsers();
        List<User> copied = UserLists.copyStructureOnly(original);
        copied.get(0).setName("佐藤");
        copied.add(new User("U003", "追加", new ArrayList<>()));
        System.out.printf("STRUCTURE listSame=%s userSame=%s originalName=%s originalSize=%d copiedSize=%d%n",
                original == copied, original.get(0) == copied.get(0),
                original.get(0).getName(), original.size(), copied.size());
    }
    // END structureDemo

    // BEGIN elementDemo
    private static void elementDemo() {
        List<User> original = sourceUsers();
        List<User> copied = UserLists.copyUsersSharingTags(original);
        copied.get(0).setName("佐藤");
        copied.get(0).getTags().add("review");
        System.out.printf("ELEMENTS userSame=%s tagsSame=%s originalName=%s copiedName=%s originalTags=%s copiedTags=%s%n",
                original.get(0) == copied.get(0),
                original.get(0).getTags() == copied.get(0).getTags(),
                original.get(0).getName(), copied.get(0).getName(),
                original.get(0).getTags(), copied.get(0).getTags());
    }
    // END elementDemo

    // BEGIN editingDemo
    private static void editingDemo() {
        List<User> original = sourceUsers();
        List<User> copied = UserLists.copyForEditing(original);
        copied.get(0).setName("佐藤");
        copied.get(0).getTags().add("review");
        System.out.printf("EDITING userSame=%s tagsSame=%s originalName=%s copiedName=%s originalTags=%s copiedTags=%s%n",
                original.get(0) == copied.get(0),
                original.get(0).getTags() == copied.get(0).getTags(),
                original.get(0).getName(), copied.get(0).getName(),
                original.get(0).getTags(), copied.get(0).getTags());
    }
    // END editingDemo

    // BEGIN readOnlyDemo
    private static void readOnlyDemo() {
        List<User> original = sourceUsers();
        List<User> readOnly = UserLists.copyReadOnlyStructure(original);
        String addResult;
        try {
            readOnly.add(new User("U003", "追加", new ArrayList<>()));
            addResult = "unexpected success";
        } catch (UnsupportedOperationException expected) {
            addResult = expected.getClass().getSimpleName();
        }
        readOnly.get(0).setName("佐藤");
        System.out.printf("READ_ONLY add=%s userSame=%s originalName=%s%n",
                addResult, original.get(0) == readOnly.get(0), original.get(0).getName());
    }
    // END readOnlyDemo
}
