package info.syachiku.order;

import java.time.LocalDate;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public class OrderSearchForm {
    private String keyword;
    private String status;
    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate fromDate;
    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate toDate;
    private String staffId;
    @Min(value = 1, message = "ページは1以上で入力してください")
    private Integer page;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    OrderSearchCondition toCondition() {
        int effectivePage = page == null ? 1 : page;
        return new OrderSearchCondition(keyword, status, fromDate, toDate, staffId, effectivePage);
    }
}
