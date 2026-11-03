package info.syachiku.validation;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public record RegisteredUser(String name, String email) {}
    private final List<RegisteredUser> users = new ArrayList<>();
    private int createCalls;

    public synchronized void create(UserForm form) {
        createCalls++;
        users.add(new RegisteredUser(form.getName(), form.getEmail()));
    }
    public synchronized List<RegisteredUser> users() { return List.copyOf(users); }
    public synchronized int createCalls() { return createCalls; }
    public synchronized void reset() { users.clear(); createCalls = 0; }
}
