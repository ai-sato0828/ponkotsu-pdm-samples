import java.util.Optional;

final class CustomerRegistration {
    record Customer(String externalId, Optional<String> displayName) {}
    interface Repository {
        void save(Customer customer);
    }

    private final Repository repository;

    CustomerRegistration(Repository repository) {
        this.repository = repository;
    }

    Customer register(String externalId, String displayName) {
        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("外部IDが必要です");
        }
        Customer customer = new Customer(externalId, Optional.ofNullable(displayName));
        repository.save(customer);
        return customer;
    }
}
