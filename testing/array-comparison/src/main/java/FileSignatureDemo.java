import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class FileSignatureDemo {
    private FileSignatureDemo() {}

    // BEGIN main
    public static void main(String[] args) {
        byte[] beforeInput = {1, 2};
        UnsafeFileSignature before = new UnsafeFileSignature(beforeInput);
        UnsafeFileSignature beforeExpected = new UnsafeFileSignature(new byte[]{1, 2});
        Set<UnsafeFileSignature> beforeSet = new HashSet<>();
        Map<UnsafeFileSignature, String> beforeMap = new HashMap<>();
        beforeSet.add(before);
        beforeMap.put(before, "stored");
        int beforeHash = before.hashCode();
        System.out.printf("BEFORE initial equalsExpected=%s setContains=%s mapValue=%s%n",
                before.equals(beforeExpected), beforeSet.contains(before), beforeMap.get(before));
        beforeInput[0] = 9;
        System.out.printf("BEFORE mutated equalsExpected=%s hashChanged=%s setContains=%s mapValue=%s%n",
                before.equals(beforeExpected), beforeHash != before.hashCode(),
                beforeSet.contains(before), beforeMap.get(before));

        byte[] afterInput = {1, 2};
        FileSignature after = new FileSignature(afterInput);
        FileSignature afterExpected = new FileSignature(new byte[]{1, 2});
        Set<FileSignature> afterSet = new HashSet<>();
        Map<FileSignature, String> afterMap = new HashMap<>();
        afterSet.add(after);
        afterMap.put(after, "stored");
        int afterHash = after.hashCode();
        System.out.printf("AFTER initial equalsExpected=%s setContains=%s mapValue=%s%n",
                after.equals(afterExpected), afterSet.contains(after), afterMap.get(after));
        afterInput[0] = 9;
        System.out.printf("AFTER mutated equalsExpected=%s hashChanged=%s setContains=%s mapValue=%s%n",
                after.equals(afterExpected), afterHash != after.hashCode(),
                afterSet.contains(after), afterMap.get(after));
    }
    // END main

    // BEGIN unsafe
    // Intentional Before: input storage is shared. Do not use as a mutable hash key.
    private static final class UnsafeFileSignature {
        private final byte[] value;

        private UnsafeFileSignature(byte[] value) {
            this.value = Objects.requireNonNull(value, "value");
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof UnsafeFileSignature other
                    && Arrays.equals(value, other.value);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }
    }
    // END unsafe
}
