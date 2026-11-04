package example.report;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

final class SalesDay {
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    static ZonedDateTime inTokyo(Instant paidAt) {
        return paidAt.atZone(TOKYO);
    }

    static LocalDate forTokyo(Instant paidAt) {
        return inTokyo(paidAt).toLocalDate();
    }
}
