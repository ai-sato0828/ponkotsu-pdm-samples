package example.registration;

import java.util.Optional;

public final class CustomerRegistration {
    public record Customer(String email) {}

    public interface CustomerRepository {
        Optional<Customer> findByEmail(String email);

        Customer save(Customer customer);
    }

    private final CustomerRepository customerRepository;

    public CustomerRegistration(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer findOrRegister(String email) {
        return customerRepository.findByEmail(email)
                .orElseGet(() -> customerRepository.save(new Customer(email)));
    }
}
