package example.shipping;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.open-in-view=false"
})
@AutoConfigureMockMvc
@Import(BeforeShippingNotesController.class)
class ShippingNoteUpdateTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private ShippingNoteRepository notes;

    @Autowired
    private PlatformTransactionManager transactions;

    private long id;

    @BeforeEach
    void newShippingNote() {
        id = notes.saveAndFlush(new ShippingNote("指定なし")).getId();
    }

    @Test
    void ignoringRequestVersionOverwritesTheFirstEdit() throws Exception {
        long screenA = openScreen();
        long screenB = openScreen();
        save("/before", screenA, "午前に配達")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.memo").value("午前に配達"));
        save("/before", screenB, "置き配")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.memo").value("置き配"));
        assertSaved("置き配", 2);
        System.out.println("before B=200 savedMemo=置き配 savedVersion=2");
    }

    @Test
    void checkingRequestVersionRejectsTheStaleEdit() throws Exception {
        long screenA = openScreen();
        long screenB = openScreen();
        save("", screenA, "午前に配達")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.memo").value("午前に配達"));
        save("", screenB, "置き配")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STALE_SHIPPING_NOTE"));
        assertSaved("午前に配達", 1);
        System.out.println("after B=409 savedMemo=午前に配達 savedVersion=1");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"memo\":\"置き配\"}",
        "{\"memo\":\"置き配\",\"version\":null}",
        "{\"memo\":\"置き配\",\"version\":-1}"
    })
    void rejectingAnInvalidVersionLeavesTheInitialMemo(String body) throws Exception {
        mvc.perform(put("/shipping-notes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest());
        assertSaved("指定なし", 0);
        System.out.println("invalid version=400 savedMemo=指定なし savedVersion=0");
    }

    @Test
    void anUnchangedMemoKeepsTheCurrentVersion() throws Exception {
        save("", 0, "指定なし")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(0));
        assertSaved("指定なし", 0);
    }

    @Test
    void jpaRejectsAnUpdateAfterAnotherTransactionHasCommitted() {
        TransactionTemplate screenB = new TransactionTemplate(transactions);
        TransactionTemplate screenA = new TransactionTemplate(transactions);
        screenA.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        assertThrows(OptimisticLockingFailureException.class, () ->
                screenB.executeWithoutResult(b -> {
                    ShippingNote stale = notes.findById(id).orElseThrow();
                    assertEquals(0L, stale.getVersion());
                    screenA.executeWithoutResult(a -> {
                        ShippingNote fresh = notes.findById(id).orElseThrow();
                        assertEquals(0L, fresh.getVersion());
                        fresh.changeMemo("午前に配達");
                        notes.flush();
                        assertEquals(1L, fresh.getVersion());
                    });

                    stale.changeMemo("置き配");
                    notes.flush();
                }));

        assertSaved("午前に配達", 1);
        System.out.println("flush conflict=OptimisticLockingFailureException savedMemo=午前に配達 savedVersion=1");
    }

    private long openScreen() throws Exception {
        byte[] body = mvc.perform(get("/shipping-notes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(0))
                .andReturn().getResponse().getContentAsByteArray();
        return json.readTree(body).get("version").asLong();
    }

    private ResultActions save(String prefix, long version, String memo) throws Exception {
        return mvc.perform(put(prefix + "/shipping-notes/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(Map.of("version", version, "memo", memo))));
    }

    private void assertSaved(String memo, long version) {
        ShippingNote saved = notes.findById(id).orElseThrow();
        assertEquals(memo, saved.getMemo());
        assertEquals(version, saved.getVersion());
    }
}
