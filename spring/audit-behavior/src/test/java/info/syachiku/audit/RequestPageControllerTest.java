package info.syachiku.audit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RequestPageControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private RequestSubmissionRepository repository;

    @BeforeEach
    void clear() {
        repository.deleteAll();
    }

    @Test
    void リダイレクト先を二度GETしても一件のまま() throws Exception {
        String location = submit("/requests", "A", "申請A");

        mvc.perform(get(location)).andExpect(status().isOk());
        mvc.perform(get(location)).andExpect(status().isOk());

        assertEquals(1, repository.count());
    }

    @Test
    void 同じ内容を二度POSTするとPRGだけでは二件保存される() throws Exception {
        submit("/requests", "A", "申請A");
        submit("/requests", "A", "申請A");

        assertEquals(2, repository.count());
    }

    @Test
    void 同じキーの同時POSTは同じ申請を指しDBに一件残る() throws Exception {
        var start = new CountDownLatch(1);
        var ready = new CountDownLatch(2);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> submitTogether(start, ready));
            var second = executor.submit(() -> submitTogether(start, ready));
            org.junit.jupiter.api.Assertions.assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            assertEquals(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
        assertEquals(1, repository.count());
    }

    @Test
    void キーを再利用して内容を変えたら409になる() throws Exception {
        submit("/requests/once", "A", "申請A");
        mvc.perform(post("/requests/once").param("requestKey", "A").param("title", "別の申請"))
                .andExpect(status().isConflict());
        assertEquals(1, repository.count());
    }

    private String submitTogether(CountDownLatch start, CountDownLatch ready) throws Exception {
        ready.countDown();
        org.junit.jupiter.api.Assertions.assertTrue(start.await(5, TimeUnit.SECONDS));
        return submit("/requests/once", "A", "申請A");
    }

    private String submit(String path, String key, String title) throws Exception {
        return mvc.perform(post(path).param("requestKey", key).param("title", title))
                .andExpect(status().isFound())
                .andReturn().getResponse().getRedirectedUrl();
    }
}
