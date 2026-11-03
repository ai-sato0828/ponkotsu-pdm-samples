package info.syachiku.samples.value;

import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LombokContractsTest {
    @Test void U01_dataPrintsAllThreeFieldsIncludingDummyHash() {
        UnsafeUser user = unsafeUser();
        assertEquals("UnsafeUser(userId=U-1, displayName=Alice, passwordHash=DUMMY-HASH-ONLY)", user.toString());
    }
    @Test void U02_dataIncludesMutableFieldInEquality() {
        UnsafeUser left = unsafeUser();
        UnsafeUser right = unsafeUser();
        assertEquals(left, right);
        right.setDisplayName("Bob");
        assertNotEquals(left, right);
    }
    @Test void U03_hashSetLookupFailsAfterEqualityFieldChanges() {
        UnsafeUser user = unsafeUser();
        Set<UnsafeUser> users = new HashSet<>();
        users.add(user);
        assertTrue(users.contains(user));
        user.setDisplayName("Bob");
        assertFalse(users.contains(user));
        assertEquals(1, users.size());
        assertSame(user, users.iterator().next());
    }
    @Test void S01_safeToStringUsesOnlyApprovedUserId() {
        SafeUser user = safeUser();
        assertEquals("SafeUser(userId=U-1)", user.toString());
        assertFalse(user.toString().contains("DUMMY-HASH-ONLY"));
        assertFalse(user.toString().contains("Alice"));
    }
    @Test void S02_sameAssignedIdIsEqualDespiteDifferentState() {
        SafeUser left = safeUser();
        SafeUser right = new SafeUser("U-1");
        right.setDisplayName("Bob");
        right.setPasswordHash("OTHER-DUMMY-HASH");
        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
    }
    @Test void S03_differentAssignedIdIsDifferentUser() {
        assertNotEquals(safeUser(), new SafeUser("U-2"));
    }
    @Test void S04_hashSetLookupSurvivesStateChanges() {
        SafeUser user = safeUser();
        Set<SafeUser> users = new HashSet<>();
        users.add(user);
        int hashBefore = user.hashCode();
        user.setDisplayName("Bob");
        user.setPasswordHash("OTHER-DUMMY-HASH");
        assertEquals(hashBefore, user.hashCode());
        assertTrue(users.contains(user));
        assertTrue(users.contains(new SafeUser("U-1")));
        assertEquals(1, users.size());
    }
    @Test void S05_unassignedIdIsRejected() {
        assertThrows(NullPointerException.class, () -> new SafeUser(null));
    }
    @Test void E01_excludeChangesOnlyToString() {
        ExcludedUser left = new ExcludedUser("U-1");
        ExcludedUser right = new ExcludedUser("U-1");
        left.setPasswordHash("DUMMY-A");
        right.setPasswordHash("DUMMY-B");
        assertFalse(left.toString().contains("DUMMY-A"));
        assertNotEquals(left, right);
    }
    @Test void R01_getterSetterDoesNotCreateValueEquality() {
        ReferenceUser left = new ReferenceUser();
        ReferenceUser right = new ReferenceUser();
        left.setUserId("U-1");
        right.setUserId("U-1");
        assertNotEquals(left, right);
        assertEquals(left, left);
    }
    @Test void G01_safeUserDoesNotHaveIdSetter() {
        assertThrows(NoSuchMethodException.class, () -> SafeUser.class.getMethod("setUserId", String.class));
    }
    private UnsafeUser unsafeUser() {
        UnsafeUser user = new UnsafeUser("U-1");
        user.setDisplayName("Alice");
        user.setPasswordHash("DUMMY-HASH-ONLY");
        return user;
    }
    private SafeUser safeUser() {
        SafeUser user = new SafeUser("U-1");
        user.setDisplayName("Alice");
        user.setPasswordHash("DUMMY-HASH-ONLY");
        return user;
    }
}
