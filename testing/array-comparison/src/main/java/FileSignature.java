import java.util.Arrays;
import java.util.Objects;

final class FileSignature {
    private final byte[] value;

    FileSignature(byte[] value) {
        this.value = Objects.requireNonNull(value, "value").clone();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FileSignature other
                && Arrays.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
