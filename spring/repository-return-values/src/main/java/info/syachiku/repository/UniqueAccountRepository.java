package info.syachiku.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UniqueAccountRepository extends JpaRepository<UniqueAccount, Long> {
    boolean existsByEmail(String email);
}
