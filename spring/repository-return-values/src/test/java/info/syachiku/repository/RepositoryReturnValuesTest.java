package info.syachiku.repository;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(showSql = false)
@Import(AccountDirectory.class)
class RepositoryReturnValuesTest {
    private static final String EMAIL = "dummy@example.invalid";
    @Autowired AccountRepository accounts;
    @Autowired UniqueAccountRepository uniqueAccounts;
    @Autowired AccountDirectory directory;

    private Account one() { return accounts.saveAndFlush(new Account(EMAIL)); }
    private void two() { one(); one(); }

    @Test void R01_optionalReturnsEmptyWhenNoRowMatches() {
        assertTrue(accounts.findOptionalByEmail(EMAIL).isEmpty());
    }

    @Test void R02_singleEntityReturnsNullWhenNoRowMatches() {
        assertNull(accounts.findSingleByEmail(EMAIL));
    }

    @Test void R03_listReturnsEmptyWhenNoRowMatches() {
        assertEquals(0, accounts.findAllByEmailOrderByIdAsc(EMAIL).size());
    }

    @Test void R04_oneRowIsTheSameAccountForAllThreeReturnTypes() {
        Account saved = one();
        assertEquals(saved.getId(), accounts.findOptionalByEmail(EMAIL).orElseThrow().getId());
        assertEquals(saved.getId(), accounts.findSingleByEmail(EMAIL).getId());
        var found = accounts.findAllByEmailOrderByIdAsc(EMAIL);
        assertEquals(1, found.size());
        assertEquals(saved.getId(), found.get(0).getId());
    }

    @Test void R05_optionalDoesNotSelectOneOfTwoRows() {
        two();
        assertThrows(IncorrectResultSizeDataAccessException.class, () -> accounts.findOptionalByEmail(EMAIL));
    }

    @Test void R06_singleEntityDoesNotSelectOneOfTwoRows() {
        two();
        assertThrows(IncorrectResultSizeDataAccessException.class, () -> accounts.findSingleByEmail(EMAIL));
    }

    @Test void R07_listReturnsBothRowsInExplicitIdOrder() {
        Account first = one();
        Account second = one();
        var found = accounts.findAllByEmailOrderByIdAsc(EMAIL);
        assertEquals(2, found.size());
        assertEquals(first.getId(), found.get(0).getId());
        assertEquals(second.getId(), found.get(1).getId());
    }

    @Test void R08_directGetFailsForAnAbsentId() {
        assertTrue(accounts.findById(-1L).isEmpty());
        assertThrowsExactly(NoSuchElementException.class, () -> accounts.findById(-1L).get());
    }

    @Test void R09_searchTreatsMissingAccountAsANormalEmptyResult() {
        assertTrue(directory.searchEmail(EMAIL).isEmpty());
    }

    @Test void R10_requiredAccountTurnsMissingIntoTheBusinessException() {
        var error = assertThrows(AccountNotFoundException.class, () -> directory.require(-1L));
        assertEquals("Account not found: -1", error.getMessage());
    }

    @Test void R11_existsAndCountDoNotHaveTheSameMeaningAsSingleResult() {
        two();
        assertTrue(accounts.existsByEmail(EMAIL));
        assertEquals(2L, accounts.countByEmail(EMAIL));
    }

    @Test void R12_uniqueConstraintRejectsTheSecondSave() {
        uniqueAccounts.saveAndFlush(new UniqueAccount(EMAIL));
        assertThrows(DataIntegrityViolationException.class, () -> uniqueAccounts.saveAndFlush(new UniqueAccount(EMAIL)));
    }

    @Test void R13_uniqueTableAcceptsOneAccount() {
        UniqueAccount saved = uniqueAccounts.saveAndFlush(new UniqueAccount(EMAIL));
        assertNotNull(saved.getId());
        assertTrue(uniqueAccounts.existsByEmail(EMAIL));
    }

    @Test void R14_priorExistenceCheckDoesNotCreateAUniquenessConstraint() {
        assertFalse(accounts.existsByEmail(EMAIL));
        one();
        one();
        assertEquals(2L, accounts.countByEmail(EMAIL));
    }
}
