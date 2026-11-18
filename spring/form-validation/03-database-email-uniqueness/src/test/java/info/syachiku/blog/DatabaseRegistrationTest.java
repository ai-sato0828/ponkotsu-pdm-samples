package info.syachiku.blog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.BindingResult;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Execution(ExecutionMode.SAME_THREAD)
class DatabaseRegistrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserService service;
    @Autowired JdbcTemplate jdbc;
    @Autowired JdbcUserRepository actualRepository;
    @Autowired ControlledRepository controlled;

    @TestConfiguration
    static class ControlConfiguration {
        @Bean @Primary
        ControlledRepository controlledRepository(JdbcUserRepository actual) {
            return new ControlledRepository(actual);
        }
    }

    // Test-only wrapper: actual SELECT finishes before waiting. Production has no gate.
    static class ControlledRepository implements UserRepository {
        private final JdbcUserRepository actual;
        final ConcurrentLinkedQueue<Boolean> existsResults = new ConcurrentLinkedQueue<>();
        final ConcurrentLinkedQueue<RuntimeException> insertFailures = new ConcurrentLinkedQueue<>();
        final AtomicInteger inserts = new AtomicInteger();
        volatile CyclicBarrier barrier;

        ControlledRepository(JdbcUserRepository actual) { this.actual = actual; }

        public boolean existsByEmail(String email) {
            boolean found = actual.existsByEmail(email);
            existsResults.add(found);
            CyclicBarrier current = barrier;
            if (current != null) {
                try { current.await(5, TimeUnit.SECONDS); }
                catch (Exception failure) { throw new IllegalStateException("test gate timed out", failure); }
            }
            return found;
        }

        public long save(UserRegisterCommand command) {
            inserts.incrementAndGet();
            try { return actual.save(command); }
            catch (RuntimeException failure) { insertFailures.add(failure); throw failure; }
        }

        void reset() {
            barrier = null;
            existsResults.clear(); insertFailures.clear(); inserts.set(0);
        }
    }

    @BeforeEach
    void cleanDatabaseOutsideAnyTestTransaction() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        controlled.reset();
        assertTrue(jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>)
                connection -> connection.getAutoCommit()));
        jdbc.update("delete from registration_users");
    }

    private UserRegisterCommand command(String name, String email) {
        return new UserRegisterCommand(name, email, "DEV");
    }

    private int rows() {
        return jdbc.queryForObject("select count(*) from registration_users", Integer.class);
    }

    private void seedDuplicate() {
        actualRepository.save(command("既存", "same@example.com"));
    }

    @Test
    void ordinaryDuplicateStopsAtThePrecheckAndHasNoDatabaseCause() {
        seedDuplicate();
        var failure = assertThrows(DuplicateEmailException.class,
                () -> service.register(command("山田", "same@example.com")));
        assertNull(failure.getCause());
        assertEquals(List.of(true), new ArrayList<>(controlled.existsResults));
        assertEquals(0, controlled.inserts.get()); assertEquals(1, rows());
        System.out.println("ORDINARY precheck=true inserts=0 cause=none rows=1");
    }

    record Registration(Long id, DuplicateEmailException failure) {}

    private Registration register(String name) {
        try { return new Registration(service.register(command(name, "same@example.com")), null); }
        catch (DuplicateEmailException failure) { return new Registration(null, failure); }
    }

    @Test
    void bothFalseChecksStillProduceOneInsertAndOneDatabaseDuplicate() throws Exception {
        controlled.barrier = new CyclicBarrier(2);
        var results = race(() -> register("山田"), () -> register("佐藤"));
        assertEquals(1, results.stream().filter(r -> r.id() != null && r.id() > 0).count());
        var loser = results.stream().filter(r -> r.failure() != null).findFirst().orElseThrow();
        assertInstanceOf(DuplicateKeyException.class, loser.failure().getCause());
        assertDatabaseRace();
        System.out.println("SERVICE_RACE exists=[false,false] created=1 duplicate=1 dbCause=DuplicateKeyException rows=1");
    }

    @Test
    void ordinaryHtmlDuplicateKeepsAllInputsAndReturnsTheEmailFieldError() throws Exception {
        seedDuplicate();
        var r = mvc.perform(post("/users").param("name", "山田").param("email", "same@example.com")
                .param("departmentCode", "DEV")).andExpect(status().isOk()).andReturn();
        assertHtmlDuplicate(r); assertEquals(0, controlled.inserts.get()); assertEquals(1, rows());
    }

    @Test
    void ordinaryApiDuplicateReturns409WithoutAttemptingAnInsert() throws Exception {
        seedDuplicate();
        mvc.perform(apiRequest("山田")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
        assertEquals(0, controlled.inserts.get()); assertEquals(1, rows());
    }

    @Test
    void htmlDatabaseRaceKeepsTheLosingRequestInputsAndUsesTheSameFieldError() throws Exception {
        controlled.barrier = new CyclicBarrier(2);
        var results = race(
                () -> mvc.perform(post("/users").param("name", "山田").param("email", "same@example.com").param("departmentCode", "DEV")).andReturn(),
                () -> mvc.perform(post("/users").param("name", "佐藤").param("email", "same@example.com").param("departmentCode", "OPS")).andReturn());
        assertEquals(List.of(200, 302), results.stream().map(r -> r.getResponse().getStatus()).sorted().toList());
        assertHtmlDuplicate(results.stream().filter(r -> r.getResponse().getStatus() == 200).findFirst().orElseThrow());
        var success = results.stream().filter(r -> r.getResponse().getStatus() == 302).findFirst().orElseThrow();
        assertEquals("/users/new?registered", success.getResponse().getRedirectedUrl());
        assertDatabaseRace();
        System.out.println("HTML_RACE exists=[false,false] statuses=[200,302] emailError=true inputsPreserved=true rows=1");
    }

    @Test
    void apiDatabaseRaceReturnsOne201AndOne409FromTheDatabaseConflict() throws Exception {
        controlled.barrier = new CyclicBarrier(2);
        var results = race(() -> mvc.perform(apiRequest("山田")).andReturn(),
                () -> mvc.perform(apiRequest("佐藤")).andReturn());
        assertEquals(List.of(201, 409), results.stream().map(r -> r.getResponse().getStatus()).sorted().toList());
        String loser = results.stream().filter(r -> r.getResponse().getStatus() == 409).findFirst().orElseThrow().getResponse().getContentAsString();
        assertTrue(loser.contains("DUPLICATE_EMAIL"));
        assertDatabaseRace();
        System.out.println("API_RACE exists=[false,false] statuses=[201,409] code=DUPLICATE_EMAIL rows=1");
    }

    @Test
    void aRealNotNullViolationIsNotConvertedToDuplicateEmail() {
        var failure = assertThrows(DataIntegrityViolationException.class,
                () -> service.register(command(null, "other@example.com")));
        assertFalse(failure instanceof DuplicateKeyException); assertEquals(0, rows());
        assertEquals(1, controlled.inserts.get());
        System.out.println("OTHER_CONSTRAINT type=DataIntegrityViolationException duplicateKey=false rows=0");
    }

    @Test
    void htmlBlankInputStopsBeforeAnyDatabaseCheck() throws Exception {
        mvc.perform(post("/users").param("name", "").param("email", ""))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form", "name", "email"));
        assertTrue(controlled.existsResults.isEmpty()); assertEquals(0, controlled.inserts.get()); assertEquals(0, rows());
    }

    @Test
    void apiInvalidEmailStopsBeforeAnyDatabaseCheck() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"山田\",\"email\":\"invalid\"}"))
                .andExpect(status().isBadRequest());
        assertTrue(controlled.existsResults.isEmpty()); assertEquals(0, rows());
    }

    @Test
    void apiSuccessReturns201AndPersistsTheCommandValues() throws Exception {
        mvc.perform(apiRequest("山田")).andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber());
        assertEquals(1, rows());
        assertEquals("山田", jdbc.queryForObject("select name from registration_users where email=?", String.class, "same@example.com"));
        assertEquals("DEV", jdbc.queryForObject("select department_code from registration_users where email=?", String.class, "same@example.com"));
    }

    @Test
    void htmlSuccessRedirectsAndPersistsTheCommandValues() throws Exception {
        mvc.perform(post("/users").param("name", "山田").param("email", "same@example.com").param("departmentCode", "DEV"))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/users/new?registered"));
        assertEquals(1, rows());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder apiRequest(String name) {
        return post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\",\"email\":\"same@example.com\",\"departmentCode\":\"DEV\"}");
    }

    private void assertHtmlDuplicate(MvcResult result) throws Exception {
        assertEquals("users/new", result.getModelAndView().getViewName());
        BindingResult errors = (BindingResult) result.getModelAndView().getModel().get(BindingResult.MODEL_KEY_PREFIX + "form");
        assertEquals("duplicate", errors.getFieldError("email").getCode());
        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains("既に使われています"));
        for (String field : List.of("name", "email", "departmentCode")) {
            assertTrue(html.contains("value=\"" + result.getRequest().getParameter(field) + "\""));
        }
    }

    private void assertDatabaseRace() {
        assertEquals(List.of(false, false), new ArrayList<>(controlled.existsResults));
        assertEquals(2, controlled.inserts.get()); assertEquals(1, rows());
        assertEquals(1, controlled.insertFailures.size());
        assertInstanceOf(DuplicateKeyException.class, controlled.insertFailures.element());
    }

    private <T> List<T> race(Callable<T> first, Callable<T> second) throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        try {
            var a = executor.submit(first);
            var b = executor.submit(second);
            return List.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS));
        } finally {
            if (controlled.barrier != null) controlled.barrier.reset();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "worker threads must stop");
            controlled.barrier = null;
        }
    }
}
