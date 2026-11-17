package info.syachiku.listcopy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class UserLists {
    private UserLists() {}

    // BEGIN structureCopy
    public static List<User> copyStructureOnly(List<User> source) {
        return new ArrayList<>(source);
    }
    // END structureCopy

    // BEGIN elementCopy
    public static List<User> copyUsersSharingTags(List<User> source) {
        return source.stream()
                .map(user -> new User(user.getId(), user.getName(), user.getTags()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    // END elementCopy

    // BEGIN editingCopy
    public static List<User> copyForEditing(List<User> source) {
        return source.stream()
                .map(user -> new User(user.getId(), user.getName(),
                        new ArrayList<>(user.getTags())))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    // END editingCopy

    // BEGIN readOnlyCopy
    public static List<User> copyReadOnlyStructure(List<User> source) {
        return List.copyOf(source);
    }
    // END readOnlyCopy
}
