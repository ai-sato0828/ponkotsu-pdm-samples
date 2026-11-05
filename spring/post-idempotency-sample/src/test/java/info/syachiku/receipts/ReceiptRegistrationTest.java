package info.syachiku.receipts;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReceiptRegistrationTest {
    private static final UUID FIRST = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID SECOND = UUID.fromString("00000000-0000-4000-8000-000000000002");
    private static final String TITLE = "備品購入申請";
    private static final String OTHER_TITLE = "別の申請";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void clearReceipts() {
        jdbc.update("DELETE FROM application_receipt");
    }

    @Test
    void sameIdAndTitleReturnsTheStoredReceipt() throws Exception {
        var first = send(FIRST, TITLE).andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value(FIRST.toString()))
                .andExpect(jsonPath("$.title").value(TITLE)).andReturn().getResponse();
        var replay = send(FIRST, TITLE).andExpect(status().isOk()).andReturn().getResponse();

        assertEquals(first.getContentAsString(), replay.getContentAsString());
        assertEquals(1, countReceipts());
    }

    @Test
    void sameIdAndDifferentTitleReturnsConflictAndPreservesOriginal() throws Exception {
        send(FIRST, TITLE).andExpect(status().isCreated());
        var conflict = send(FIRST, OTHER_TITLE).andExpect(status().isConflict()).andReturn().getResponse();

        assertFalse(conflict.getContentAsString().contains(TITLE));
        assertEquals(TITLE, jdbc.queryForObject(
                "SELECT title FROM application_receipt WHERE request_id = ?", String.class, FIRST));
        assertEquals(1, countReceipts());
    }

    @Test
    void differentIdsAreSeparateReceiptsEvenWithSameTitle() throws Exception {
        send(FIRST, TITLE).andExpect(status().isCreated());
        send(SECOND, TITLE).andExpect(status().isCreated());

        assertEquals(2, countReceipts());
    }

    @Test
    void invalidRequestIsRejectedBeforeInsert() throws Exception {
        send(FIRST, "   ").andExpect(status().isBadRequest());
        assertEquals(0, countReceipts());
    }

    @Test
    void concurrentIdenticalPostsCreateOneRowAndReturnTheSameReceipt() throws Exception {
        var start = new CyclicBarrier(2);
        Callable<Reply> sendPost = () -> {
            start.await(5, TimeUnit.SECONDS);
            var response = send(FIRST, TITLE).andReturn().getResponse();
            return new Reply(response.getStatus(), response.getContentAsString());
        };
        var replies = runTogether(sendPost);

        assertEquals(List.of(200, 201), replies.stream().map(Reply::status).sorted().toList());
        assertEquals(replies.get(0).body(), replies.get(1).body());
        assertEquals(1, countReceipts());
    }

    @Test
    void bothConnectionsCanSeeNoRowBeforeOnlyOneInsertSucceeds() throws Exception {
        var bothRead = new CyclicBarrier(2);
        var transactions = new TransactionTemplate(transactionManager);
        Callable<Attempt> checkThenInsert = () -> {
            int[] sessionId = new int[1];
            try {
                transactions.executeWithoutResult(status -> {
                    sessionId[0] = jdbc.queryForObject("SELECT SESSION_ID()", Integer.class);
                    assertEquals(0, countReceipts());
                    await(bothRead);
                    jdbc.update("INSERT INTO application_receipt (request_id, title) VALUES (?, ?)", FIRST, TITLE);
                });
                return new Attempt(sessionId[0], "inserted");
            } catch (DuplicateKeyException ex) {
                return new Attempt(sessionId[0], "duplicate");
            }
        };
        var attempts = runTogether(checkThenInsert);

        assertEquals(2, attempts.stream().map(Attempt::sessionId).distinct().count());
        assertEquals(List.of("duplicate", "inserted"),
                attempts.stream().map(Attempt::result).sorted().toList());
        assertEquals(1, countReceipts());
    }

    private ResultActions send(UUID requestId, String title) throws Exception {
        String json = """
                {"requestId":"%s","title":"%s"}
                """.formatted(requestId, title);
        return mvc.perform(post("/applications").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long countReceipts() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM application_receipt", Long.class);
    }

    private static void await(CyclicBarrier barrier) {
        try {
            barrier.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static <T> List<T> runTogether(Callable<T> work) throws Exception {
        var workers = Executors.newFixedThreadPool(2);
        try {
            var first = workers.submit(work);
            var second = workers.submit(work);
            return List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
        } finally {
            workers.shutdownNow();
        }
    }

    private record Reply(int status, String body) {
    }

    private record Attempt(int sessionId, String result) {
    }
}
