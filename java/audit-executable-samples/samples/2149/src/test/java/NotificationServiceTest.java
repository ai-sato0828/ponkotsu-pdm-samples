import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationServiceTest {
    @Test
    void sendsOneFormattedMessageAndReturnsSuccess() {
        List<String> sent = new ArrayList<>();
        var service = new NotificationService(sent::add);

        String result = service.notify("処理完了");

        assertEquals("送信済み", result);
        assertEquals(List.of("通知: 処理完了"), sent);
    }

    @Test
    void propagatesTheSenderFailure() {
        var failure = new IllegalStateException("送信先で失敗");
        List<String> attempted = new ArrayList<>();
        var service = new NotificationService(text -> {
            attempted.add(text);
            throw failure;
        });

        assertSame(failure, assertThrows(IllegalStateException.class, () -> service.notify("処理完了")));
        assertEquals(List.of("通知: 処理完了"), attempted);
    }

    @Test
    void doesNotSendAnEmptySubject() {
        List<String> sent = new ArrayList<>();
        var service = new NotificationService(sent::add);

        assertThrows(IllegalArgumentException.class, () -> service.notify(" "));
        assertEquals(List.of(), sent);
    }
}
