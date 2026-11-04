package info.syachiku.form;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.stereotype.Service;

@Service
class UserService {
    private final Queue<Registration> registrations = new ConcurrentLinkedQueue<>();

    void register(String name, String department, int seatLimit) {
        registrations.add(new Registration(name, department, seatLimit));
    }

    record Registration(String name, String department, int seatLimit) {}
}
