package info.syachiku.blog;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // BEGIN register
    public long register(UserRegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException();
        }
        try {
            return userRepository.save(command);
        } catch (DuplicateKeyException failure) {
            // This INSERT has exactly one uniqueness rule: the email primary key.
            throw new DuplicateEmailException(failure);
        }
    }
    // END register
}
