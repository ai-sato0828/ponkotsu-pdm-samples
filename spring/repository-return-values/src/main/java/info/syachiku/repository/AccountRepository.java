package info.syachiku.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findOptionalByEmail(String email);
    Account findSingleByEmail(String email);
    List<Account> findAllByEmailOrderByIdAsc(String email);
    boolean existsByEmail(String email);
    long countByEmail(String email);
}
