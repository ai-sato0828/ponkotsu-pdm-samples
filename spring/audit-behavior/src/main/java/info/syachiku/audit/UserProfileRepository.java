package info.syachiku.audit;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
