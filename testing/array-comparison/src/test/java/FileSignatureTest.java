import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileSignatureTest {
    @Test
    void separateArraysWithTheSameContentsHaveEqualSignaturesAndHashes() {
        FileSignature first = new FileSignature(new byte[]{1, 2});
        FileSignature second = new FileSignature(new byte[]{1, 2});
        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equivalentSignatureCanFindSetAndMapEntries() {
        FileSignature stored = new FileSignature(new byte[]{1, 2});
        FileSignature lookup = new FileSignature(new byte[]{1, 2});
        assertTrue(Set.of(stored).contains(lookup));
        assertEquals("stored", Map.of(stored, "stored").get(lookup));
    }

    @Test
    void changingTheInputAfterConstructionDoesNotChangeTheSignature() {
        byte[] input = {1, 2};
        FileSignature signature = new FileSignature(input);
        int originalHash = signature.hashCode();
        input[0] = 9;
        assertEquals(new FileSignature(new byte[]{1, 2}), signature);
        assertEquals(originalHash, signature.hashCode());
        assertNotEquals(new FileSignature(new byte[]{9, 2}), signature);
    }

    @Test
    void changingTheInputDoesNotLoseHashSetAndHashMapEntries() {
        byte[] input = {1, 2};
        FileSignature signature = new FileSignature(input);
        Set<FileSignature> signatures = new HashSet<>();
        Map<FileSignature, String> stored = new HashMap<>();
        signatures.add(signature);
        stored.put(signature, "stored");
        input[0] = 9;
        assertTrue(signatures.contains(signature));
        assertTrue(signatures.contains(new FileSignature(new byte[]{1, 2})));
        assertEquals("stored", stored.get(signature));
        assertEquals("stored", stored.get(new FileSignature(new byte[]{1, 2})));
    }

    @Test
    void nullInputIsRejectedAtConstruction() {
        assertThrows(NullPointerException.class, () -> new FileSignature(null));
    }

    @Test
    void differentContentsOrderAndLengthAreNotEqual() {
        FileSignature signature = new FileSignature(new byte[]{1, 2});
        assertNotEquals(signature, new FileSignature(new byte[]{1, 3}));
        assertNotEquals(signature, new FileSignature(new byte[]{2, 1}));
        assertNotEquals(signature, new FileSignature(new byte[]{1}));
    }
}
