package info.syachiku.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    private final RegisteredUserRepository users;
    private final UserProfileRepository profiles;

    RegistrationService(RegisteredUserRepository users, UserProfileRepository profiles) {
        this.users = users;
        this.profiles = profiles;
    }

    public void registerWithoutTransaction(String name, String handle) {
        saveUserAndProfile(name, handle);
    }

    @Transactional
    public void register(String name, String handle) {
        saveUserAndProfile(name, handle);
    }

    private void saveUserAndProfile(String name, String handle) {
        RegisteredUser user = users.save(new RegisteredUser(name));
        profiles.save(new UserProfile(user.getId(), handle));
    }
}
