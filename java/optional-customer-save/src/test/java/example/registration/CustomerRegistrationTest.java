package example.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import example.registration.CustomerRegistration.Customer;
import example.registration.CustomerRegistration.CustomerRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CustomerRegistrationTest {
    private static final String EMAIL = "customer@example.test";

    @Test
    void existingCustomerIsReturnedWithoutSaving() {
        Customer existing = new Customer(EMAIL);
        CountingRepository repository = new CountingRepository(existing, false);
        CustomerRegistration service = new CustomerRegistration(repository);

        assertSame(existing, service.findOrRegister(EMAIL));
        assertEquals(0, repository.saveCalls);
    }

    @Test
    void existingCustomerDoesNotTriggerDuplicateSaveError() {
        Customer existing = new Customer(EMAIL);
        CountingRepository repository = new CountingRepository(existing, true);
        CustomerRegistration service = new CustomerRegistration(repository);

        assertSame(existing, service.findOrRegister(EMAIL));
        assertEquals(0, repository.saveCalls);
    }

    @Test
    void missingCustomerIsSavedOnceAndReturned() {
        CountingRepository repository = new CountingRepository(null, true);
        CustomerRegistration service = new CustomerRegistration(repository);

        Customer result = service.findOrRegister(EMAIL);

        assertEquals(EMAIL, result.email());
        assertSame(repository.stored, result);
        assertEquals(1, repository.saveCalls);
    }

    private static final class CountingRepository implements CustomerRepository {
        private Customer stored;
        private final boolean rejectDuplicates;
        private int saveCalls;

        CountingRepository(Customer stored, boolean rejectDuplicates) {
            this.stored = stored;
            this.rejectDuplicates = rejectDuplicates;
        }

        @Override
        public Optional<Customer> findByEmail(String email) {
            return Optional.ofNullable(stored).filter(customer -> customer.email().equals(email));
        }

        @Override
        public Customer save(Customer customer) {
            saveCalls++;
            if (rejectDuplicates && customer.equals(stored)) {
                throw new IllegalStateException("同じメールアドレスを登録できません");
            }
            stored = customer;
            return customer;
        }
    }
}
