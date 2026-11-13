package info.syachiku.order500;

import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;

@Component
public class NotificationStub {
    public void send(OrderView order, boolean fail) {
        if (fail) {
            // ローカル例外モデル。ソケットも通知先への送信も作らない。
            throw new IllegalStateException("local notification stub failed",
                    new SocketTimeoutException("simulated notification timeout"));
        }
    }
}
