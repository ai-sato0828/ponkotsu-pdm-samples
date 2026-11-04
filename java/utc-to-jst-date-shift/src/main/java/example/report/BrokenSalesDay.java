package example.report;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

final class BrokenSalesDay {
    static ZonedDateTime inTokyo(Instant paidAt) {
        LocalDateTime utcClock = LocalDateTime.ofInstant(paidAt, ZoneOffset.UTC);
        return utcClock.atZone(ZoneId.of("Asia/Tokyo"));
    }
}
