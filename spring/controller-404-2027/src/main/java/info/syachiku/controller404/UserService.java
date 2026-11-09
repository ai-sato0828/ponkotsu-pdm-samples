package info.syachiku.controller404;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final AtomicInteger calls = new AtomicInteger();

    public UserResponse findById(Long id) {
        calls.incrementAndGet();
        if (id == 10L) {
            return new UserResponse(10L, "Tanaka");
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
    }

    // 教材の到達確認用。実業務では適切なログや監視を使う。
    public int calls() {
        return calls.get();
    }

    public void resetCalls() {
        calls.set(0);
    }
}
