package info.syachiku.reference;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerQueryService {
    private final CustomerRepository repository;

    CustomerQueryService(CustomerRepository repository) {
        this.repository = repository;
    }

    // BEGIN requireCustomerName
    @Transactional(readOnly = true)
    public String requireCustomerName(long id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return customer.getName();
    }
    // END requireCustomerName
}
