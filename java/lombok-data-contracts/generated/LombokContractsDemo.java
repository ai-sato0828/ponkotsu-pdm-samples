package info.syachiku.samples.value;

import java.util.HashSet;
import java.util.Set;

public final class LombokContractsDemo {
    public static void main(String[] args) {
        UnsafeUser unsafe = new UnsafeUser("U-1");
        unsafe.setDisplayName("Alice");
        unsafe.setPasswordHash("DUMMY-HASH-ONLY");
        System.out.println("unsafe=" + unsafe);
        Set<UnsafeUser> unsafeSet = new HashSet<>();
        unsafeSet.add(unsafe);
        System.out.println("unsafe.contains.before=" + unsafeSet.contains(unsafe));
        unsafe.setDisplayName("Bob");
        System.out.println("unsafe.contains.after=" + unsafeSet.contains(unsafe));
        System.out.println("unsafe.size=" + unsafeSet.size());
        System.out.println("unsafe.iterator.sameObject=" + (unsafeSet.iterator().next() == unsafe));

        SafeUser safe = new SafeUser("U-1");
        safe.setDisplayName("Alice");
        safe.setPasswordHash("DUMMY-HASH-ONLY");
        Set<SafeUser> safeSet = new HashSet<>();
        safeSet.add(safe);
        safe.setDisplayName("Bob");
        System.out.println("safe=" + safe);
        System.out.println("safe.contains.after=" + safeSet.contains(safe));
        System.out.println("safe.sameIdDifferentState=" + safe.equals(new SafeUser("U-1")));
        ExcludedUser excluded = new ExcludedUser("U-1");
        excluded.setPasswordHash("DUMMY-HASH-ONLY");
        System.out.println("excluded=" + excluded);
    }
}
