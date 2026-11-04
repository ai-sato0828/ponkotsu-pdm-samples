package info.syachiku.audit;

import org.springframework.data.jpa.repository.JpaRepository;

interface RegisteredUserRepository extends JpaRepository<RegisteredUser, Long> {
}
