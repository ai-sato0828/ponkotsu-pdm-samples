import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public class LombokToStringExample {
    @Data
    @ToString(onlyExplicitlyIncluded = true)
    static class ParentDto {
        @ToString.Include
        private final String userId;
        private final String privateNote;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    static class ChildDefault extends ParentDto {
        private final String role;

        ChildDefault(String userId, String privateNote, String role) {
            super(userId, privateNote);
            this.role = role;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true, onlyExplicitlyIncluded = true)
    static class ChildWithParent extends ParentDto {
        @ToString.Include
        private final String role;

        ChildWithParent(String userId, String privateNote, String role) {
            super(userId, privateNote);
            this.role = role;
        }
    }

    public static void main(String[] args) {
        String defaultText = new ChildDefault("u-7", "hidden", "operator").toString();
        String withParent = new ChildWithParent("u-7", "hidden", "operator").toString();

        if (defaultText.contains("u-7") || !withParent.contains("userId=u-7")
                || !withParent.contains("role=operator") || withParent.contains("hidden")) {
            throw new AssertionError(defaultText + " / " + withParent);
        }
        System.out.println(defaultText);
        System.out.println(withParent);
    }
}
