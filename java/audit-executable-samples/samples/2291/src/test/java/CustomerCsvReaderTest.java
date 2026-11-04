import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerCsvReaderTest {
    @TempDir Path temp;

    @Test
    void preservesCommaNewlineAndQuotes() throws Exception {
        var path = Path.of(getClass().getResource("/customers.csv").toURI());

        assertEquals(List.of(
                new CustomerCsvReader.Customer("1", "山田, 太郎", "1行目\n2行目"),
                new CustomerCsvReader.Customer("2", "佐藤", "\"要確認\"")),
                CustomerCsvReader.read(path));
    }

    @Test
    void rejectsDuplicateHeader() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> CustomerCsvReader.read(csv("id,name,name\n1,A,n\n")));
    }

    @Test
    void rejectsWrongHeaderOrder() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> CustomerCsvReader.read(csv("name,id,note\nA,1,n\n")));
    }

    @Test
    void rejectsMissingColumn() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> CustomerCsvReader.read(csv("id,name,note\n1,A\n")));
    }

    @Test
    void rejectsBlankRequiredValue() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> CustomerCsvReader.read(csv("id,name,note\n1, ,n\n")));
    }

    @Test
    void rejectsBomUnderThisImportContract() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> CustomerCsvReader.read(csv("\uFEFFid,name,note\n1,A,n\n")));
    }

    private Path csv(String content) throws Exception {
        return Files.writeString(temp.resolve("input.csv"), content);
    }
}
