package info.syachiku.order;

import java.time.LocalDate;

record OrderSearchCondition(String keyword, String status, LocalDate fromDate,
                            LocalDate toDate, String staffId, int page) {

    OrderSearchCondition {
        keyword = normalize(keyword);
        status = normalize(status);
        staffId = normalize(staffId);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
