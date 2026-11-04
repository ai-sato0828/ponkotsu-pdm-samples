package example.notification;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.AfterTransaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

@JdbcTest
class NotificationTimeoutTest {
    private static final Duration LIMIT = Duration.ofSeconds(10);

    @Autowired
    private JdbcTemplate jdbc;

    private long rowId;
    private int expectedAfterRollback;

    @Test
    void preemptiveTimeoutLeavesTheInsertedRow() {
        rowId = 100;
        expectedAfterRollback = 1;
        Thread testThread = Thread.currentThread();

        Thread insertThread = assertTimeoutPreemptively(LIMIT, () -> insertLog(rowId));

        System.out.printf("id=%d sameThread=%b%n", rowId, testThread == insertThread);
        assertEquals(1, countRows());
    }

    @Test
    void sameThreadTimeoutRollsBackTheInsertedRow() {
        rowId = 200;
        expectedAfterRollback = 0;
        Thread testThread = Thread.currentThread();

        Thread insertThread = assertTimeout(LIMIT, () -> insertLog(rowId));

        System.out.printf("id=%d sameThread=%b%n", rowId, testThread == insertThread);
        assertEquals(1, countRows());
    }

    @AfterTransaction
    void checkAndCleanAfterRollback() {
        try {
            int remaining = countRows();
            System.out.printf("id=%d rowsAfterRollback=%d%n", rowId, remaining);
            assertEquals(expectedAfterRollback, remaining, "rows after the test transaction");
        } finally {
            jdbc.update("DELETE FROM notification_log WHERE id = ?", rowId);
        }
    }

    private Thread insertLog(long id) {
        jdbc.update("INSERT INTO notification_log (id, message) VALUES (?, ?)", id, "reservation confirmed");
        return Thread.currentThread();
    }

    private int countRows() {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM notification_log WHERE id = ?", Integer.class, rowId);
    }
}

@SpringBootApplication
class NotificationTestApplication {}
