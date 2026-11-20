package info.syachiku.rollback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest(
        classes = ReceiptServiceTest.SampleApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:checked_rollback",
            "spring.sql.init.mode=always"
        })
class ReceiptServiceTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(ReceiptService.class)
    static class SampleApplication {}

    @Autowired ReceiptService service;
    @Autowired JdbcTemplate jdbc;

    @Test
    void successCommits() throws IOException {
        String orderId = "success";
        verifyOutsideTransaction();
        service.register(orderId, () -> verifyInsertBeforeOutput(orderId));
        verifyRowsAfterCall(orderId, 1, "none");
    }

    @Test
    void uncheckedExceptionRollsBackByDefault() {
        String orderId = "unchecked-default";
        var failure = new IllegalStateException("模擬した出力エラー");
        verifyOutsideTransaction();
        var actual = assertThrowsExactly(IllegalStateException.class,
                () -> service.register(orderId, () -> {
                    verifyInsertBeforeOutput(orderId);
                    throw failure;
                }));
        assertSame(failure, actual);
        verifyRowsAfterCall(orderId, 0, actual.getClass().getSimpleName());
    }

    @Test
    void checkedExceptionCommitsByDefault() {
        String orderId = "checked-default";
        var failure = new IOException("模擬した出力エラー");
        verifyOutsideTransaction();
        var actual = assertThrowsExactly(IOException.class,
                () -> service.register(orderId, () -> {
                    verifyInsertBeforeOutput(orderId);
                    throw failure;
                }));
        assertSame(failure, actual);
        verifyRowsAfterCall(orderId, 1, actual.getClass().getSimpleName());
    }

    @Test
    void checkedExceptionRollsBackWithTypedRule() {
        String orderId = "checked-rollback-for";
        var failure = new IOException("模擬した出力エラー");
        verifyOutsideTransaction();
        var actual = assertThrowsExactly(IOException.class,
                () -> service.registerRollbackOnIo(orderId, () -> {
                    verifyInsertBeforeOutput(orderId);
                    throw failure;
                }));
        assertSame(failure, actual);
        verifyRowsAfterCall(orderId, 0, actual.getClass().getSimpleName());
    }

    private void verifyOutsideTransaction() {
        assertTrue(AopUtils.isAopProxy(service), "Springのプロキシ経由でサービスを呼ぶ");
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive(),
                "呼び出し元のテストはトランザクションの外");
    }

    private void verifyInsertBeforeOutput(String orderId) {
        assertTrue(TransactionSynchronizationManager.isActualTransactionActive(),
                "INSERT後の出力処理まで同じトランザクションの内側");
        assertEquals(1, rowsFor(orderId), "出力が失敗する前にINSERTは済んでいる");
    }

    private void verifyRowsAfterCall(String orderId, int expected, String exception) {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive(),
                "サービスから戻った後にDBを読む");
        int actual = rowsFor(orderId);
        assertEquals(expected, actual);
        System.out.printf("%s: exception=%s, rows=%d%n", orderId, exception, actual);
    }

    private int rowsFor(String orderId) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM receipt_registrations WHERE order_id = ?",
                Integer.class, orderId);
    }
}
