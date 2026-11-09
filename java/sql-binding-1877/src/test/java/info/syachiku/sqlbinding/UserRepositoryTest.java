package info.syachiku.sqlbinding;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {
    private DataSource database;
    private CountingDataSource counting;
    private UserRepository repository;

    @BeforeEach
    void setUp() {
        database = SampleDatabase.create();
        counting = new CountingDataSource(database);
        repository = new UserRepository(counting);
    }

    static List<String> ids(List<User> users) {
        return users.stream().map(User::id).toList();
    }

    @Test
    void S01_通常名は連結版もバインド版も同じ1行() {
        assertEquals(List.of("U002"), ids(new UnsafeUserRepository(database).findByName("Alice")));
        assertEquals(List.of("U002"), ids(repository.findByName("Alice")));
    }

    @Test
    void S02_引用符は連結版で例外となりバインド版はU001() {
        assertThrows(DataAccessException.class,
                () -> new UnsafeUserRepository(database).findByName("O'Brien"));
        assertEquals(List.of("U001"), ids(repository.findByName("O'Brien")));
        assertEquals("obrien@example.invalid", repository.findByName("O'Brien").get(0).email());
    }

    @Test
    void S03_条件式風入力は連結版で全5行となりバインド版は0行() {
        String input = "' OR '1'='1";
        assertEquals(List.of("U001", "U002", "U003", "U004", "U005"),
                ids(new UnsafeUserRepository(database).findByName(input)));
        assertTrue(repository.findByName(input).isEmpty());
    }

    @Test
    void S04_生JDBCも引用符と条件式を値として扱う() throws Exception {
        PlainJdbcUserRepository plain = new PlainJdbcUserRepository(counting);
        assertEquals(List.of("U001"), ids(plain.findByName("O'Brien")));
        assertTrue(plain.findByName("' OR '1'='1").isEmpty());
    }

    @Test
    void S05_日付は境界当日を含み名前付き版も同じ3行() {
        LocalDate from = LocalDate.of(2026, 5, 2);
        List<String> expected = List.of("U002", "U004", "U005");
        assertEquals(expected, ids(repository.findByStatusFrom("ACTIVE", from)));
        assertEquals(expected, ids(repository.findByStatusFromNamed("ACTIVE", from)));
    }

    @Test
    void S06_名前付き条件へ渡した条件式風statusは0行() {
        assertTrue(repository.findByStatusFromNamed("' OR '1'='1", LocalDate.of(2026, 1, 1)).isEmpty());
    }

    @Test
    void S07_LIKEは部分一致として扱う() {
        assertEquals(List.of("U001"), ids(repository.findByNameLike("Brien")));
    }

    @Test
    void S08_LIKEのパーセントは全5行に一致する() {
        assertEquals(5, repository.findByNameLike("%").size());
    }

    @Test
    void S09_LIKEのアンダースコアもワイルドカードになる() {
        assertEquals(5, repository.findByNameLike("_").size());
        assertEquals(List.of("U005"), ids(repository.findByNameLike("A_B")));
    }

    @Test
    void S10_LIKEでも条件式風入力はSQL条件にならない() {
        assertTrue(repository.findByNameLike("' OR '1'='1").isEmpty());
    }

    @Test
    void S11_空文字は完全一致0行でLIKE全5行() {
        assertTrue(repository.findByName("").isEmpty());
        assertEquals(5, repository.findByNameLike("").size());
    }

    @Test
    void S12_名前列上限の64文字を保存して検索できる() {
        String name = "a".repeat(64);
        new JdbcTemplate(database).update("INSERT INTO users VALUES (?, ?, ?, ?, ?)",
                "U006", name, "long@example.invalid", "ACTIVE", java.sql.Date.valueOf("2026-05-05"));
        assertEquals(List.of("U006"), ids(repository.findByName(name)));
        assertTrue(repository.findByName("a".repeat(65)).isEmpty());
    }

    @Test
    void S13_列名nameとcreatedAtは固定候補で並べる() {
        assertEquals(List.of("U004", "U005", "U002", "U003", "U001"), ids(repository.findAllSorted("name", true)));
        assertEquals(List.of("U005", "U004", "U002", "U001", "U003"), ids(repository.findAllSorted("createdAt", false)));
    }

    @Test
    void S14_不明なソート値とnullはid順へ戻す() {
        List<String> expected = List.of("U001", "U002", "U003", "U004", "U005");
        assertEquals(expected, ids(repository.findAllSorted("name DESC; DROP TABLE users", true)));
        assertEquals(expected, ids(repository.findAllSorted(null, true)));
        assertEquals(List.of("U005", "U004", "U003", "U002", "U001"), ids(repository.findAllSorted("unknown", false)));
    }

    @Test
    void S15_INは重複IDと未登録IDを含めても2行() {
        assertEquals(List.of("U001", "U003"), ids(repository.findByIds(List.of("U003", "U001", "U001", "NONE"))));
    }

    @Test
    void S16_空INは空結果で接続取得も0回() {
        assertTrue(repository.findByIds(List.of()).isEmpty());
        assertEquals(0, counting.connectionRequests());
    }

    static Stream<Arguments> nullInputs() {
        return Stream.of(
                Arguments.of("name", (Consumer<UserRepository>) r -> r.findByName(null)),
                Arguments.of("keyword", (Consumer<UserRepository>) r -> r.findByNameLike(null)),
                Arguments.of("status", (Consumer<UserRepository>) r -> r.findByStatusFrom(null, LocalDate.of(2026, 5, 2))),
                Arguments.of("from", (Consumer<UserRepository>) r -> r.findByStatusFrom("ACTIVE", null)),
                Arguments.of("named status", (Consumer<UserRepository>) r -> r.findByStatusFromNamed(null, LocalDate.of(2026, 5, 2))),
                Arguments.of("named from", (Consumer<UserRepository>) r -> r.findByStatusFromNamed("ACTIVE", null)),
                Arguments.of("ids", (Consumer<UserRepository>) r -> r.findByIds(null)),
                Arguments.of("id element", (Consumer<UserRepository>) r -> r.findByIds(Arrays.asList("U001", null))));
    }

    @ParameterizedTest(name = "S17 null {0}")
    @MethodSource("nullInputs")
    void S17_null入力を接続前に拒否(String label, Consumer<UserRepository> action) {
        assertThrows(NullPointerException.class, () -> action.accept(repository));
        assertEquals(0, counting.connectionRequests());
    }

    @Test
    void S18_不明な状態と最終登録より後の日付は0行() {
        assertTrue(repository.findByStatusFrom("", LocalDate.of(2026, 1, 1)).isEmpty());
        assertTrue(repository.findByStatusFromNamed("UNKNOWN", LocalDate.of(2026, 1, 1)).isEmpty());
        assertTrue(repository.findByStatusFromNamed("ACTIVE", LocalDate.of(2026, 5, 5)).isEmpty());
    }

    @Test
    void S19_生JDBCもnull名を接続前に拒否() {
        PlainJdbcUserRepository plain = new PlainJdbcUserRepository(counting);
        assertThrows(NullPointerException.class, () -> plain.findByName(null));
        assertEquals(0, counting.connectionRequests());
    }
}
