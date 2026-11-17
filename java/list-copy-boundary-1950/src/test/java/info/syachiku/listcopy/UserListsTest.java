package info.syachiku.listcopy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserListsTest {
    private static List<User> users() {
        return new ArrayList<>(List.of(
                new User(new String("U001"), new String("田中"),
                        new ArrayList<>(List.of("general", "pending"))),
                new User(new String("U002"), new String("鈴木"),
                        new ArrayList<>(List.of("support")))));
    }

    private static List<String> ids(List<User> users) {
        return users.stream().map(User::getId).toList();
    }

    @Test
    void structureCopyCreatesOnlyANewList() {
        List<User> original = users();
        List<User> copied = UserLists.copyStructureOnly(original);
        assertNotSame(original, copied);
        assertEquals(List.of("U001", "U002"), ids(copied));
        for (int i = 0; i < original.size(); i++) {
            assertSame(original.get(i), copied.get(i));
            assertSame(original.get(i).getTags(), copied.get(i).getTags());
        }
    }

    @Test
    void structureAddAndRemoveDoNotChangeTheOtherList() {
        List<User> original = users();
        List<User> copied = UserLists.copyStructureOnly(original);
        copied.remove(0);
        copied.add(new User("U003", "追加", new ArrayList<>()));
        assertEquals(List.of("U001", "U002"), ids(original));
        original.remove(1);
        original.add(new User("U004", "元へ追加", new ArrayList<>()));
        assertEquals(List.of("U002", "U003"), ids(copied));
    }

    @Test
    void structureCopyNameChangesFlowInBothDirections() {
        List<User> original = users();
        List<User> copied = UserLists.copyStructureOnly(original);
        copied.get(0).setName("佐藤");
        assertEquals("佐藤", original.get(0).getName());
        original.get(1).setName("岡田");
        assertEquals("岡田", copied.get(1).getName());
    }

    @Test
    void userOnlyCopyPreservesEveryFieldButSharesTags() {
        List<User> original = users();
        List<User> copied = UserLists.copyUsersSharingTags(original);
        assertNotSame(original, copied);
        for (int i = 0; i < original.size(); i++) {
            assertNotSame(original.get(i), copied.get(i));
            assertEquals(original.get(i).getId(), copied.get(i).getId());
            assertEquals(original.get(i).getName(), copied.get(i).getName());
            assertEquals(original.get(i).getTags(), copied.get(i).getTags());
            assertSame(original.get(i).getTags(), copied.get(i).getTags());
        }
    }

    @Test
    void userOnlyCopyNameChangesStayIndependentInBothDirections() {
        List<User> original = users();
        List<User> copied = UserLists.copyUsersSharingTags(original);
        copied.get(0).setName("佐藤");
        assertEquals("田中", original.get(0).getName());
        original.get(1).setName("岡田");
        assertEquals("鈴木", copied.get(1).getName());
    }

    @Test
    void userOnlyCopyTagsStillLeakInBothDirections() {
        List<User> original = users();
        List<User> copied = UserLists.copyUsersSharingTags(original);
        copied.get(0).getTags().add("review");
        assertEquals(List.of("general", "pending", "review"), original.get(0).getTags());
        original.get(1).getTags().add("source-change");
        assertEquals(List.of("support", "source-change"), copied.get(1).getTags());
    }

    @Test
    void editingCopyPreservesAllFieldsAndOnlySharesImmutableStrings() {
        List<User> original = users();
        List<User> copied = UserLists.copyForEditing(original);
        assertNotSame(original, copied);
        for (int i = 0; i < original.size(); i++) {
            User before = original.get(i);
            User after = copied.get(i);
            assertNotSame(before, after);
            assertNotSame(before.getTags(), after.getTags());
            assertEquals(before.getId(), after.getId());
            assertEquals(before.getName(), after.getName());
            assertEquals(before.getTags(), after.getTags());
            assertSame(before.getId(), after.getId());
            assertSame(before.getName(), after.getName());
            assertSame(before.getTags().get(0), after.getTags().get(0));
        }
    }

    @Test
    void editingCopyNameAndTagChangesDoNotLeakEitherWay() {
        List<User> original = users();
        List<User> copied = UserLists.copyForEditing(original);
        copied.get(0).setName("佐藤");
        copied.get(0).getTags().add("review");
        assertEquals("田中", original.get(0).getName());
        assertEquals(List.of("general", "pending"), original.get(0).getTags());
        original.get(1).setName("岡田");
        original.get(1).getTags().add("source-change");
        assertEquals("鈴木", copied.get(1).getName());
        assertEquals(List.of("support"), copied.get(1).getTags());
    }

    @Test
    void editingCopyListStructureIsAlsoIndependentInBothDirections() {
        List<User> original = users();
        List<User> copied = UserLists.copyForEditing(original);
        copied.remove(0);
        copied.add(new User("U003", "追加", new ArrayList<>()));
        assertEquals(List.of("U001", "U002"), ids(original));
        original.remove(1);
        original.add(new User("U004", "元へ追加", new ArrayList<>()));
        assertEquals(List.of("U002", "U003"), ids(copied));
    }

    @Test
    void copyOfRejectsStructuralChangesButStillSharesMutableUsers() {
        List<User> original = users();
        List<User> readOnly = UserLists.copyReadOnlyStructure(original);
        assertThrows(UnsupportedOperationException.class,
                () -> readOnly.add(new User("U003", "追加", new ArrayList<>())));
        assertThrows(UnsupportedOperationException.class, () -> readOnly.remove(0));
        original.add(new User("U004", "元へ追加", new ArrayList<>()));
        assertEquals(2, readOnly.size());
        assertSame(original.get(0), readOnly.get(0));
        readOnly.get(0).setName("佐藤");
        assertEquals("佐藤", original.get(0).getName());
        original.get(0).getTags().add("source-change");
        assertEquals(List.of("general", "pending", "source-change"), readOnly.get(0).getTags());
    }
}
