import java.util.List;
import java.util.Objects;

public class ListContainsIdDemo {
    record UserEntity(Long id) {
        Long getId() {
            return id;
        }
    }

    record UserDto(Long id) {}

    static boolean containsIdBefore(List<UserEntity> users, UserDto target) {
        return users.stream()
                .anyMatch(user -> Objects.equals(user.getId(), target.id()));
    }

    static boolean containsAssignedId(List<UserEntity> users, UserDto target) {
        Long targetId = target.id();
        return targetId != null && users.stream()
                .anyMatch(user -> targetId.equals(user.getId()));
    }

    public static void main(String[] args) {
        var savedUsers = List.of(new UserEntity(10L));
        var mixedUsers = List.of(new UserEntity(10L), new UserEntity(null));

        check("同じ採番済みID", savedUsers, new UserDto(10L), true, true);
        check("異なる採番済みID", savedUsers, new UserDto(20L), false, false);
        check("採番済みの一覧と未採番の検索対象", savedUsers, new UserDto(null), false, false);
        check("候補と検索対象の両方に未採番ID", mixedUsers, new UserDto(null), true, false);
    }

    private static void check(String name, List<UserEntity> users, UserDto target,
                              boolean expectedBefore, boolean expectedAfter) {
        boolean before = containsIdBefore(users, target);
        boolean after = containsAssignedId(users, target);
        if (before != expectedBefore || after != expectedAfter) {
            throw new AssertionError(name + " expected " + expectedBefore + "/" + expectedAfter
                    + ", got " + before + "/" + after);
        }
        System.out.printf("%s 旧=%b 修正=%b%n", name, before, after);
    }
}
